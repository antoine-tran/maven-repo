package tuan.ml;

/** This contains features of a document where access to
 * a feature values at a specific dimension is guaranteed
 * to be (pseudo-) random access*/
public interface RandomAccessFeatures extends Features {

	/** retrieve the feature quantified value at a specific dimension, or 
	 * Double.MIN_VALUE if not found */
	public double featureValueAtDimension(int dimension);
	
	/** get feature values of a document at a speficic dimension. Note that not
	 * all implementations support this operation */
	public Object featureAtDimension(int dimension) throws UnsupportedOperationException;
	
	/** update the feature value at a specific index. The index here is the 
	 * local index, not the global one */
	public void updateLocalFeature(int index, double value);
	
	/** update the feature nominal value at a specific index. The index here is the 
	 * local index, not the global one */
	public void updateLocalFeature(int index, Object value);
}
