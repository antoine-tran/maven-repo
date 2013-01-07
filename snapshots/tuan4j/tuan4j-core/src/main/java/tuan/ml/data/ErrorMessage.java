/**
 * 
 */
package tuan.ml.data;

/**
 * This class contains all error message constants
 * @author tuan
 *
 */
public enum ErrorMessage {
	
	DIMENSION_INDEX_OUT_OF_BOUND ("The dimension index is out of bound"),
	DIMENSION_INDEX_UNMAPPED ("The dimensions have to be mapped to contiguous integers"),
	FEATURES_NULL ("The features set of a document cannot be null"),
	FEATURES_NULL_EXPLAINED ("The features set of a document %s cannot be null"),
	DOC_KEY_NULL ("Document must have non-null key"),
	DIMENSION_FEATURE_UNMATCHED("Dimension and feature sizes must match");
	
	private String value;
	private ErrorMessage(String value) {
		this.value = value;
	}
	
	public String toString() {
		return value;
	}
}
