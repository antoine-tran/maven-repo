package tuan.lucene;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;

import com.google.common.collect.HashMultiset;

import tuan.io.FileUtility;
import tuan.ml.data.ArrayFeatures;
import tuan.ml.data.Document;

public class TermFeatureExtractor {

	/** Default field */
	private static final String DEFAULT_FIELD = "text";

	/** List of fields indexed in the lucene store */
	private Set<String> fields;

	/** List of all terms in a corpus. We use a primitive hash
	 * to support O(1) term seek time, and use Trove4j to save
	 * memory use*/
	private TObjectIntHashMap<String> vocabulary;

	private IndexReader index;

	public TermFeatureExtractor(String lucenePath) throws IOException {
		this(lucenePath, (String[])null);
	}

	public TermFeatureExtractor(String luceneFilePath, String... inputFields) throws IOException {
		Directory dir = FSDirectory.open(new File(luceneFilePath));
		index = DirectoryReader.open(dir);
		if (inputFields != null) {
			fields = new HashSet<String>(inputFields.length);
			for (String inputField : inputFields) fields.add(inputField);
		}
		else {
			fields = new HashSet<String>();
			fields.add(DEFAULT_FIELD);
		}
	}

	public void loadVocabulary(String vocabularyFile) throws IOException {
		if (vocabularyFile != null) {
			vocabulary = new TObjectIntHashMap<String>();
			int i = 1;
			for (String term : FileUtility.readLines(vocabularyFile, null)) {
				vocabulary.put(term, i++);
			}
		}
		else loadVocabulary();
	}

	/** Extract list of vocabulary and load into the main memory
	 * @throws IOException 
	 */
	protected void loadVocabulary() throws IOException {
		vocabulary = new TObjectIntHashMap<String>();
		int totalCnt = 0;
		BytesRef term;
		
		for (String field : fields) {
			Terms termsAtField = MultiFields.getTerms(index, field);
			TermsEnum termsEnum = termsAtField.iterator(null);
			while ((term = termsEnum.next()) != null) {
				String termText = term.utf8ToString();
				vocabulary.putIfAbsent(termText, totalCnt++);
			}
		}
	}

	/** Extract list of vocabulary and load into the main memory
	 * Caveat: We must call this method separately from loadVocabulary(),
	 * for the consistency of global dimension mappings
	 */
	public void exportVocabulary(String outputFile) throws IOException {
		FileWriter writer = null;
		try {
			writer = new FileWriter(outputFile);
			vocabulary = new TObjectIntHashMap<String>();
			TermsEnum termsEnum = null;
			for (String field : fields) {
				Terms termsAtField = MultiFields.getTerms(index, field);
				termsEnum = termsAtField.iterator(termsEnum);
			}
			BytesRef term;
			int totalCnt = 0;
			while ((term = termsEnum.next()) != null) {
				String termText = term.utf8ToString();
				if (!vocabulary.containsKey(termText)) {
					writer.write(termText + "\n");
					vocabulary.put(termText, totalCnt++);
				}
			}
		} finally {
			if (writer != null) writer.close();
		}
	}

	/** Extract features for all document, and push result to a stream 
	 * @throws IOException */
	public void extractTFIDF(String vocabularyFile, Writer output) throws IOException {
		loadVocabulary(vocabularyFile);
		Bits liveDocs = MultiFields.getLiveDocs(index);
		for (int i = 0, n = index.numDocs(); i < n; i++) {
			if (liveDocs == null || liveDocs.get(i)) {
				Document doc = tfidf(i);
				output.write(doc.key() + "\t" + doc.toString() + "\n");
			}
		}
		output.flush();
	}

	/** Extract features for a document indexed by lucene. The input 
	 * is document id in the index, the output is a Document object, 
	 * where dimensions are terms, and features are tf-idf 
	 * @throws IOException */
	public Document tfidf(int docNo) throws IOException {
		return tfidf(docNo, fields); 
	}

	/** Extract features for a document indexed by lucene and output as 
	 * a string. The input is document id in the index, the output is
	 * of form "f1 f2 f3 ....." 
	 * @throws IOException */
	public String extractTFIDF(int docNo) throws IOException {
		Document doc = tfidf(docNo);
		return doc.toString();
	}

	/** Extract features for a document indexed by lucene. The input 
	 * is document id in the index, and the field of the documents to be counted. The output is a
	 * Document object, where dimensions are terms, and features are tf-idf 
	 * @throws IOException */
	Document tfidf(int docNo, Set<String> fields) throws IOException {
		TermsEnum termsEnum = null;

		// if vocabulary is empty, load all terms from the index
		if (vocabulary == null) loadVocabulary();

		// accumulate terms for all the fields
		for (String field : fields) {
			Terms termsAtField = index.getTermVector(docNo, field);
			// Ignore if the term vector is not indexed. 
			// TODO: Might need to replace by a dummy TermsEnum
			if (termsAtField != null) {
				TermsEnum tmp = termsAtField.iterator(termsEnum);
				termsEnum = tmp;	
			}
		}
		if (termsEnum == null) throw new RuntimeException("Cannot extract features for a document #"
				+ docNo + ". One possible cause is terms or indices are not properly indexed !");

		HashMultiset<String> tmpTerms  = HashMultiset.create();
		TIntDoubleHashMap tfidf = new TIntDoubleHashMap();

		// first run: calculate the idf's
		int totalDocs = index.numDocs();
		BytesRef term;
		while ((term = termsEnum.next()) != null) {
			String t = term.utf8ToString();
			tmpTerms.add(t);

			if (!vocabulary.containsKey(t)) throw new RuntimeException("Found unknown term: " + t);
			int docFreq = termsEnum.docFreq();
			double idf = 1 + Math.log(totalDocs / (double) docFreq);
			tfidf.putIfAbsent(vocabulary.get(t), idf);
		}

		// second run: update with the tf's
		Set<String> uniqueTerms = tmpTerms.elementSet();
		int[] dimension = new int[uniqueTerms.size()];
		int totalFreq = tmpTerms.size();
		int i = 0;
		for (String t : uniqueTerms) {
			int d = vocabulary.get(t);
			int termFreq = tmpTerms.count(t);
			double tf = termFreq / (double) totalFreq;
			double idf = tfidf.get(d);
			tfidf.put(d, tf * idf);
			dimension[i++] = d;
		}

		// construct the output 
		Arrays.sort(dimension);
		double[] features = new double[i];
		for (i = 0; i < features.length; i++) {
			features[i] = tfidf.get(dimension[i]);
		}
		ArrayFeatures f = new ArrayFeatures(dimension, features);
		return new Document(String.valueOf(docNo), f, vocabulary.size());
	}
}