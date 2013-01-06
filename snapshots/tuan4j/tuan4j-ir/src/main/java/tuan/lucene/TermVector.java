package tuan.lucene;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import tuan.ir.core.Document;

public class TermVector {

	/** Default field */
	private static final String DEFAULT_FIELD = "text";

	/** List of fields indexed in the lucene store */
	private Set<String> fields;
	
	/** List of all terms in a corpus. We use a primitive hash
	 * to support O(1) term seek time, and to save memory use*/
	private TObjectIntHashMap<String> terms;

	private IndexReader index;
	
	public TermVector(String lucenePath) throws IOException {
		this(lucenePath, null);
	}

	public TermVector(String luceneFilePath, @Nullable Set<String> defaultField) throws IOException {
		Directory dir = FSDirectory.open(new File(luceneFilePath));
		index = DirectoryReader.open(dir);
		if (defaultField != null) fields = defaultField;
		else {
			fields = new HashSet<String>();
			fields.add(DEFAULT_FIELD);
		}
	}

	/** Extract features for a document indexed by lucene. The input 
	 * is document id in the index, the output is a Document object, 
	 * where dimensions are terms, and features are tf-idf 
	 * @throws IOException */
	public Document tfIdf(int docNo) throws IOException {
		return tfIdf(docNo, fields); 
	}

	/** Extract features for a document indexed by lucene. The input 
	 * is document id in the index, and the field of the documents to be counted. The output is a
	 * Document object, where dimensions are terms, and features are tf-idf 
	 * @throws IOException */
	public Document tfIdf(int docNo, Set<String> fields) throws IOException {
		org.apache.lucene.document.Document doc = index.document(docNo, fields);		
		TermsEnum termsEnum = null;
		
		// try to estimate the number of terms in the document. TODO: Lucene 4.0 or later might
		// have API to access this info, but I currently don't know of any
		int termsCnt = 0;
		
		// accumulate terms for all the fields
		for (String field : fields) {
			Terms terms = index.getTermVector(docNo, field);
			// Ignore if the term vector is not indexed. 
			// TODO: Might replace by a dummy TermsEnum
			if (terms != null) {
				TermsEnum tmp = terms.iterator(termsEnum);
				termsEnum = tmp;	
				termsCnt += terms.size();
			}
		}
		if (termsEnum == null) throw new RuntimeException("Cannot extract features for a document #"
				+ docNo + ". One possible cause is the term vector is not properly indexed !");
		BytesRef term;
		while ((term = termsEnum.next()) != null) {
			// TODO: do something here
		}
	}
}
