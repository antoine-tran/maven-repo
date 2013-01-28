/**
 * 
 */
package tuan.ml;

import java.io.Serializable;

import javax.annotation.Nullable;

/**
 * This is a generic document in high-dimensional space. 
 * Every document is uniquely identified by one key of String type (key
 * cannot be null) 
 * @author tuan
 *
 */
public class Document implements Comparable<Document>, Serializable {

	/**
	 * Automatically generated serialID 
	 */
	private static final long serialVersionUID = 2280379642789656553L;
	
	protected String key; 
	protected Features features;
	protected int dim;
	
	private String cachedString; 
	/** Once we have calculated the hash code of a document, we cache it for 
	 * subsequent references */
	private int hashcode;
	
	public String key() {
		return key;
	}	
		
	/** Get the document's feature */
	public Features features() {
		return features;
	}
	
	/** get the dimension size of the space containing the document */
	public int dim() {
		return dim;
	}
	
	/** Set the document's feature object */
	public void setFeatures(Features features) {
		this.features = features;
	}
	
	public Document(String key, Features features, int dim) {
		this.key = key;
		this.features = features;
		this.dim = dim;
	}
	
	/**
	 * A convenience method that transforms a string of form "dimension:feature
	 * <TAB>dimension:features"
	 * into a Document object. The document's key is automatically generated.
	 * Dimension size of the corpus has to be specified
	 */
	public Document(String features, int dim) {
		if (features == null || features.isEmpty()) throw new NullPointerException();
		String[] vals = features.split("\t");
		int n = vals.length;
		int[] dimIndex = new int[n / 2];
		double[] featVal = new double[n / 2];
		for (int i = 0, j = 0; i < dimIndex.length; i++) {
			dimIndex[i] = Integer.parseInt(vals[j++]);
			featVal[i] = Double.parseDouble(vals[j++]);
		}
		this.features = new ArrayFeatures(dimIndex, featVal, dim);
		this.key = String.valueOf(System.currentTimeMillis());
		this.dim = dim;
	}
	
	/**
	 * A convenience method that transforms a string of form "dimension:feature
	 * <SEPARATOR>dimension:features"
	 * into a Document object. The document's key is automatically generated.
	 * Dimension size of the corpus has to be specified
	 */
	public Document(String features, String delimiter, int dim) {
		if (features == null || features.isEmpty()) throw new NullPointerException();
		String[] vals = features.split(delimiter);
		int n = vals.length;
		int[] dimIndex = new int[n / 2];
		double[] featVal = new double[n / 2];
		for (int i = 0, j = 0; i < n / 2; i++) {
			dimIndex[i] = Integer.parseInt(vals[j++]);
			featVal[i] = Double.parseDouble(vals[j++]);
		}
		this.features = new ArrayFeatures(dimIndex, featVal, n / 2);
		this.key = String.valueOf(System.currentTimeMillis());
		this.dim = dim;
	}
	
	@Override
	/** sort documents by some indicator (e.g. signature length). CONVENTION: 
	 * If a document is null, then always return Integer.MIN_VALUE (all non-null
	 * documents are sorted before null document) */
	public int compareTo(@Nullable Document doc) {
		if (doc == null) {
			return Integer.MIN_VALUE;
		} 
		String docKey = doc.key();
		if (docKey == null)
			throw new NullPointerException(ErrorMessage.DOC_KEY_NULL.toString());
		Features docFeatures = doc.features();
		if (docFeatures == null)
			throw new NullPointerException(String.format(
					ErrorMessage.FEATURES_NULL_EXPLAINED.toString(), docKey));
				
		int docDim = docFeatures.size();
		int thisDim = features.size();
		
		if (docDim > thisDim) return 1;
		else if (docDim == thisDim) return 0;
		else return -1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Document)) {
			return false;
		}
		Document doc = (Document)obj;
		if (key == null) return false;
		return key.equals(doc.key);
	}
	
	@Override
	public int hashCode() {
		if (hashcode != -1) {
			return hashcode;
		}		
		// in order to get hashcode of a document, the document must have 
		// non-null feature sets
		hashcode = features.hashCode();
		return hashcode;
	}
	
	@Override
	public String toString() {
		if (cachedString == null) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0, j = 0; i < dim && j < features.size(); i++) {
				if (i == features.dimensionValue(j)) {
					sb.append(features.featureValue(j));
					j++;
				} else sb.append("0.0");
				sb.append("\t");
			}
			cachedString = sb.toString();	
		}
		return cachedString;
	}
}
