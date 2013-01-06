package tuan.ir.core;

import javax.annotation.Nullable;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is the basic implementation of Features where data are stored in 
 * primitive arrays 
 */
public class ArrayFeatures implements Features {

	/** the number of dimensions */
	private int dim;

	/** We use contiguous arrays to store features' values */
	
	// this stores id of dimensions. It has to be sorted in ascendant order
	// NOTE: At the beginning, all dimension index are set to Integer.MAX_VALUE
	private int keys[];
	
	// this stores values at a specific dimension. The value Double.MIN_VALUE
	// indicates the absence of features at a dimension (over-fitting issue),
	// the value Double.MAX_VALUE indicates the index is out of dimension
	// range (unallocated issue)
	private double vals[];
	
	public ArrayFeatures(int[] keys, double[] vals, int dim) {
		this.keys = keys;
		this.vals = vals;
		this.dim = dim;
	}

	@Override
	public String dimension(int index) {
		index = checkElementIndex(index, dim, 
				ErrorMessage.DIMENSION_INDEX_OUT_OF_BOUND.toString());

		// TODO: We do not check the null of keys here to improve the performance
		// This might be a gotcha
		return String.valueOf(keys[index]);
	}
	
	@Override
	public int dimensionValue(int index) {
		index = checkElementIndex(index, dim, 
				ErrorMessage.DIMENSION_INDEX_OUT_OF_BOUND.toString());
		
		// TODO: We do not check the null of keys here to improve the performance
		// This might be a gotcha
		// keys = checkNotNull(keys, ErrorMessage.DIMENSION_INDEX_UNMAPPED);		
		return keys[index];
	}

	@Override
	public @Nullable Object feature(int index) {
		index = checkElementIndex(index, dim, 
				ErrorMessage.DIMENSION_INDEX_OUT_OF_BOUND.toString());
		
		// TODO: We do not check the null of keys here to improve the performance
		// This might be a gotcha
		//vals = checkNotNull(vals, ErrorMessage.FEATURES_NULL);
		return (vals[index] == Double.MIN_VALUE) ? null : vals[index];
	}

	@Override
	public double featureValue(int index) {
		index = checkElementIndex(index, dim, 
				ErrorMessage.DIMENSION_INDEX_OUT_OF_BOUND.toString());
		
		// TODO: We do not check the null of keys here to improve the performance
		// This might be a gotcha
		//vals = checkNotNull(vals, ErrorMessage.FEATURES_NULL);
		return vals[index];
	}

	@Override
	public int size() {
		return dim;
	}

	@Override
	public int hashCode() {
		vals = checkNotNull(vals, ErrorMessage.FEATURES_NULL);
		int hashcode = 0;
		for (double val : vals) {
			hashcode += (int) val;
		}
		return hashcode;
	}
}
