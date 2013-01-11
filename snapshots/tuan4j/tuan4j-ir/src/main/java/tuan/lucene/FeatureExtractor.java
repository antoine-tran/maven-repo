package tuan.lucene;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;

import com.google.common.collect.HashMultiset;

import tuan.io.FileUtility;
import tuan.io.Log;
import tuan.ml.data.ArrayFeatures;
import tuan.ml.data.Document;

public class FeatureExtractor {

	/** Default field */
	private static final String DEFAULT_FIELD = "text";

	/** List of fields indexed in the lucene store */
	private Set<String> fields;

	/** List of all terms in a corpus. We use a primitive hash
	 * to support O(1) term seek time, and use Trove4j to save
	 * memory use*/
	private TObjectIntHashMap<String> vocabulary;

	private IndexReader index;

	public FeatureExtractor(String lucenePath) throws IOException {
		this(lucenePath, (String[])null);
	}

	public FeatureExtractor(String luceneFilePath, String... inputFields) throws IOException {
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
				if (!vocabulary.containsKey(termText)) {
					vocabulary.put(termText, totalCnt);
					totalCnt++;
				}				
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
			int totalCnt = 0;
			writer = new FileWriter(outputFile);
			vocabulary = new TObjectIntHashMap<String>();
			TermsEnum termsEnum = null;
			for (String field : fields) {
				Terms termsAtField = MultiFields.getTerms(index, field);
				termsEnum = termsAtField.iterator(termsEnum);
				BytesRef term;			
				while ((term = termsEnum.next()) != null) {
					String termText = term.utf8ToString();
					if (!vocabulary.containsKey(termText)) {
						writer.write(termText + "\n");
						vocabulary.put(termText, totalCnt++);
					}
				}
			}			
		} finally {
			if (writer != null) writer.close();
		}
	}

	/** Extract list of vocabulary and load into the main memory
	 * Caveat: We must call this method separately from loadVocabulary(),
	 * for the consistency of global dimension mappings
	 */
	public void exportWordDistribution(String outputFile, String contentField) throws IOException {

		// load temporarily of word and its doc frequencies
		Terms termsAtField = MultiFields.getTerms(index, contentField);
		TermsEnum termsEnum = termsAtField.iterator(null);
		BytesRef term;
		ArrayList<TermFreq> tmpArray = new ArrayList<FeatureExtractor.TermFreq>();
		while ((term = termsEnum.next()) != null) {
			long docFreq = termsEnum.docFreq();
			Term t = new Term(contentField, term.utf8ToString());
			tmpArray.add(new TermFreq(t, term.utf8ToString(), docFreq));
		}

		// sort the freqMap based on doc frequencies
		Collections.sort(tmpArray);

		// load sorted vocabulary to memory and emit to file at the same time
		int totalCnt = 0;
		FileWriter writer = null;
		try {
			writer = new FileWriter(outputFile);
			vocabulary = new TObjectIntHashMap<String>();
			for (TermFreq tf : tmpArray) {
				vocabulary.put(tf.term.text(), totalCnt++);
				long termFreq = index.totalTermFreq(tf.term);
				writer.write(tf.term + "\t" + termFreq + "\t" + tf.freq + "\n");
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
				if (doc != null) output.write(doc.key() + "\t" + doc.toString() + "\n");

				// log the document having no extracted terms for analysis
				else Log.log("document ignored " + i);
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

	/** Convenience method to extract tf-idf features for a document and output as 
	 * a string. The input is document id in the index, the output is
	 * of form "f1 f2 f3 ....." 
	 * @throws IOException */
	public String extractTFIDF(String vocabularyFile, int docNo) throws IOException {
		loadVocabulary(vocabularyFile);
		Document doc = tfidf(docNo);
		return doc.toString();
	}

	/** Extract features for a document indexed by lucene. The input  is document id in the
	 * index, and the field of the documents to be counted. The output is a Document object,
	 * where dimensions are terms, and features are tf-idf. If all terms in the document are
	 * not included in the index (e.g. document has only stop words), the null object will be
	 * returned
	 * @throws IOException */
	Document tfidf(int docNo, Set<String> fields) throws IOException {
		if (vocabulary == null) loadVocabulary();

		HashMultiset<String> tmpTerms  = HashMultiset.create();
		TIntDoubleHashMap tfidf = new TIntDoubleHashMap();

		// first run: calculate the idf's
		int totalDocs = index.numDocs();
		for (String field : fields) {

			// NOTE: Not all fields support term vecctor indexing 
			Terms termsAtField = index.getTermVector(docNo, field);

			// Ignore if the term vector is not indexed
			// TODO: When this happens, we should find other ways to retrieve
			// terms per document. This should be added in next release of 
			// TermFeatureExtractor
			if (termsAtField == null) continue;
			TermsEnum termsEnum = termsAtField.iterator(null);

			// Ignore if the terms enum is null (probably because all terms in the 
			// field of the document are not included in the vocabulary)
			if (termsEnum == null) continue;

			BytesRef term;
			while ((term = termsEnum.next()) != null) {
				String t = term.utf8ToString();

				// ignore terms that are not in the vocabulary				
				if (!vocabulary.containsKey(t)) continue;
				tmpTerms.add(t);
				int docFreq = termsEnum.docFreq();
				double idf = 1 + Math.log(totalDocs / (double) docFreq);
				tfidf.putIfAbsent(vocabulary.get(t), idf);
			}
		}

		// second run: update with the tf's
		int totalFreq = tmpTerms.size();

		// If all terms in the document are not included in the index (e.g. document has
		// only stop words), the null object will be returned
		if (totalFreq == 0) return null;

		Set<String> uniqueTerms = tmpTerms.elementSet();
		int n = uniqueTerms.size();				
		int[] dimension = new int[n];
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

	// command line tool
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		Options opts = new Options();
		OptionGroup optGrp = new OptionGroup();

		// Output stream and error stream
		Option outputStr =  OptionBuilder.withArgName("o").withLongOpt("output")
				.withDescription("When set, the system will redirect the output" +
						" stream to the file specified by this arguments")
						.hasArg()
						.create();
		opts.addOption(outputStr);

		Option errStr =  OptionBuilder.withArgName("e").withLongOpt("error")
				.withDescription("When set, the system will redirect the error" +
						" stream to the file specified by this arguments")
						.hasArg()
						.create();
		opts.addOption(errStr);

		// Option 1: load input lucene path
		Option lucenePath = OptionBuilder.withArgName("p").withLongOpt("lucene")
				.withDescription("Register lucene index path")
				.isRequired(true)
				.hasArg()				
				.create();
		optGrp.addOption(lucenePath);

		// Option 2: load input fields
		Option inputFields = OptionBuilder.withArgName("f").withLongOpt("fields")
				.withDescription("Load indexable fields that the tool will work with")
				.isRequired(true)				
				.hasOptionalArgs()
				.withValueSeparator(' ')
				.create();
		optGrp.addOption(inputFields);

		// Option 3: export vocabulary to file
		Option exportVocabulary = OptionBuilder.withArgName("v").withLongOpt("vocabulary")
				.withDescription("export vocabulary of input fields to a text file, one" +
						" term per line")
						.hasArg()
						.create();
		optGrp.addOption(exportVocabulary);

		// Option 4: export word distribution to file
		Option distribution = OptionBuilder.withArgName("d").withLongOpt("distribution")
				.withDescription("export word distributions of input fields to a text file," +
						" each line of which corresponds to a term, followed by total term" +
						" frequency and the number of documents containing the terms. " +
						"Input required: Paths of vocabulary file and output file ")
						.hasArgs(2)
						.withValueSeparator(' ')
						.create();
		optGrp.addOption(distribution);

		// Option 5: extract tf-idf features to file
		Option tfidfs = OptionBuilder.withArgName("t").withLongOpt("tfidfs")
				.withDescription("extract tf-idf vectors for documents from lucene index." +
						" Input required: paths of vocabulary file, output file")
						.hasArgs(2)
						.withValueSeparator(' ')
						.create();
		optGrp.addOption(tfidfs);

		// Option 6: extract tf-idf features for a specific document 
		Option tfidf = OptionBuilder.withArgName("i").withLongOpt("tfidf")
				.withDescription("extract tf-idf vectors for a document from lucene index." +
						" Input required: paths of vocabulary file, document id in the index")
						.hasArgs(2)
						.withValueSeparator(' ')
						.create();
		optGrp.addOption(tfidf);
		opts.addOptionGroup(optGrp);

		FeatureExtractor fe = null;
		String luceneLoc = null;
		String[] fields = null;

		// Parse the command line
		try {
			// check which script to be called
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(opts, args);


			// Print help message when command arguments are invalid
			if (cmd.getOptions().length == 0) {
				printHelp("arguments cannot be empty", opts);
				System.exit(-1);
			}			

			// Check the output / error configuration
			if (cmd.hasOption("output")) {
				String outStream = cmd.getOptionValue("output");
				PrintStream out = new PrintStream(new FileOutputStream(outStream));
				System.setOut(out);
			}

			if (cmd.hasOption("error")) {
				String errStream = cmd.getOptionValue("error");
				PrintStream err = new PrintStream(new FileOutputStream(errStream));
				System.setErr(err);
			}
			// Get mandatory argument values
			if (cmd.hasOption("lucene")) {
				luceneLoc = cmd.getOptionValue("lucene");
			} 
			else {
				printHelp("lucene index path has to be specified", opts);
				System.exit(-1);
			}

			if (cmd.hasOption("fields")) {
				fields = cmd.getOptionValues("fields");
			} 
			else {
				printHelp("lucene fields have to be specified", opts);
				System.exit(-1);
			}

			fe = new FeatureExtractor(luceneLoc, fields);

			// do the demanded tasks
			// export vocabulary
			if (cmd.hasOption("vocabulary")) {
				String input = cmd.getOptionValue("vocabulary");
				fe.exportVocabulary(input);
			}

			// export word distribution
			if (cmd.hasOption("distribution")) {
				String[] inputs = cmd.getOptionValues("distribution");
				if (inputs == null || inputs.length != 2) {
					printHelp("word distribution task needs two inputs (vocabulary" +
							" file & field)", opts);
					System.exit(-1);
				}
				else fe.exportWordDistribution(inputs[0], inputs[1]);
			}

			// extract tf-idfs for all
			if (cmd.hasOption("tfidfs")) {
				String[] inputs = cmd.getOptionValues("tfidfs");
				if (inputs == null || inputs.length != 2) {
					printHelp("tf-idf calculation task needs two inputs (vocabulary" +
							" file path, output file path)", opts);
					System.exit(-1);
				}
				else {
					Writer out = new FileWriter(inputs[1]);
					try {
						fe.extractTFIDF(inputs[0], out);
					} 
					catch (IOException e) {
						e.printStackTrace();
					}
					finally {
						out.close();
					}
				}				

			} 

			// extract tf-idf for a given document
			if (cmd.hasOption("tfidf")) {
				String[] inputs = cmd.getOptionValues("tfidf");
				if (inputs == null || inputs.length != 2) {
					printHelp("document tf-idf calculation task needs two" +
							" inputs (vocabulary file path & document id)", opts);
					System.exit(-1);
				}
				else {
					try {
						int i = Integer.parseInt(inputs[1]);
						System.out.println(fe.extractTFIDF(inputs[0], i));
					}
					catch (NumberFormatException e) {
						System.err.println("document id must be an integer");
						printHelp("document id must be an integer", opts);
						System.exit(-1);
					}
				}
			}
			/*else {
				printHelp("command line syntax", opts);
				System.exit(-1);
			}*/
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static final void printHelp(String msg, Options opts) {
		HelpFormatter help = new HelpFormatter();
		help.printHelp(msg, opts);
	}

	private static class TermFreq implements Comparable<TermFreq> {

		private String txt;
		private Term term;
		private long freq;

		public TermFreq(Term term, String txt, long freq) {
			this.term = term;
			this.txt = txt;
			this.freq = freq;
		}

		@Override
		public int compareTo(TermFreq o) {
			if (o == null) return -1;
			else if (freq > o.freq) return -1;
			else if (freq < o.freq) return 1;
			else return (o.term.compareTo(term));
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) return true;
			if (o == null || !(o instanceof TermFreq)) return false;
			TermFreq tf = (TermFreq)o;
			boolean res = ((term == null &&  tf.term == null) || term.equals(tf.term));
			res &= (freq == tf.freq);
			return res;
		}
	}
}