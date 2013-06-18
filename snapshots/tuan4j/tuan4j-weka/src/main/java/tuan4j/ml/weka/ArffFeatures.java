package tuan4j.ml.weka;

import tuan.ml.Features;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.SparseInstance;

/** A data structure that adapts between Tuan4j's Features and Weka's Instance interfaces */
public class ArffFeatures extends Instance implements Features {

	public ArffFeatures(int numAttrs) {
		super(numAttrs);
	}
	
	/** the "internal" dimension size (i.e. the number of present features) */
	public int size() {
		return numValues();
	}

	/** get the dimension name at a specific position */
	public String dimension(int index) {
		Attribute att = attributeSparse(index);
		return att.name();
	}

	/** normally in high-dimension computing, dimensions are compressed to
	 * contiguous integers. This method returns the mapping from one
	 * dimension at a specific position to its global integer values in the 
	 * dimension index */
	public int dimensionValue(int index) {
		return index(index);
	}
	
	/** get feature quantified values of a document at a specific position */
	public double featureValue(int index) {
		return m_AttValues[index];
	}
	
	/** get feature values of a document at a position. Note that not
	 * all implementations support this operation */
	public Object feature(int index) {
		return attributeSparse(index);
	}
	
	/** update the feature value at a specific dim. The index here is the 
	 * global index, not the local one */
	public void update(int dim, double value) {
		setValue(dim, value);
	}
		
	/** update a feature at a dimension and update the local index at the same time.
	 * Note that not all implementations support this operation */
	public void add(int dim, double value) throws UnsupportedOperationException {
		
		// override the existing value
		if (!isMissing(dim)) {
			update(dim, value);
		} 
		
		// add exactly one slot for the new feature value
		else {
			insertAttributeAt(dim);
			setValue(dim, value);
		}
	}
	
	/** update the feature nominal value at a specific dim. The index here is the 
	 * global index, not the local one */
	public void update(int dim, Object value) {
		setValue(dim, value.toString());
	}
		
	/** update a nominal feature at a dimension and update the local index at the same time.
	 * Note that not all implementations support this operation */
	public void add(int dim, Object value) throws UnsupportedOperationException {
		
		// override the existing value
		if (!isMissing(dim)) {
			update(dim, value.toString());
		} 
		
		// add exactly one slot for the new feature value
		else {
			insertAttributeAt(dim);
			setValue(dim, value.toString());
		}
	}
}
