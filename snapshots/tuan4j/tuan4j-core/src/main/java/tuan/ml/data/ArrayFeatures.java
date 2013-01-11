package tuan.ml.data;

import java.util.Arrays;
import java.util.BitSet;

import javax.annotation.Nullable;

/**
 * This is the basic implementation of Features where data are stored in 
 * primitive arrays. It sorts the dimension ids so that the access to
 * feature values at a specific dimension can be done in O(logn), where
 * n is a number of dimensions with positive feature values of a document
 */
public class ArrayFeatures implements RandomAccessFeatures {

	/** the number of dimensions */
	private int dim;

	/** We use contiguous arrays to store features' values */
	
	// this stores id of dimensions. It has to be sorted in ascendant order
	private int keys[];
	
	// this stores values at a specific dimension. The value Double.MIN_VALUE
	// indicates the absence of features at a dimension (over-fitting issue),
	private double vals[];
	
	/** 
	 * This is the default constructor for array-based features. The best practice
	 * is to sort dimension ids before instantiating the feature object. For the 
	 * sake of memory usage and to maintain the immutability of programs, all arrays
	 * are copied and compacted to dim size (it implicitly assumes the dimension does
	 * not change subsequently)
	 */
	public ArrayFeatures(int[] keys, double[] vals, int dim) {
		if (keys == null) 
			throw new NullPointerException(ErrorMessage.DOC_KEY_NULL.toString());
		if (vals == null) 
			throw new NullPointerException(ErrorMessage.FEATURES_NULL.toString());
		System.arraycopy(keys, 0, this.keys, 0, dim);
		System.arraycopy(vals, 0, this.vals, 0, dim);
		this.dim = dim;
	}
	
	/** 
	 * This is the quick constructor for array-based features. It follows the same
	 * best behaviours and develops the same best practices as 
	 * #tuan.ir.core.ArrayFeatures(int[], double[], int), except that the arrays are
	 * not copied. This constructor should be used with HIGH caution, as odd things
	 * might happen with mutability of parameters. 
	 */
	public ArrayFeatures(int[] keys, double[] vals) {
		if (keys == null) 
			throw new NullPointerException(ErrorMessage.DOC_KEY_NULL.toString());
		if (vals == null) 
			throw new NullPointerException(ErrorMessage.FEATURES_NULL.toString());
		if (keys.length != vals.length)
			throw new IllegalArgumentException(
					ErrorMessage.DIMENSION_FEATURE_UNMATCHED.toString());
		this.keys = keys;
		this.vals = vals;
		this.dim = keys.length;
	}
	
	public ArrayFeatures(double... featureVals) {
		if (featureVals == null)
			throw new NullPointerException(ErrorMessage.FEATURES_NULL.toString());
		BitSet flags = new BitSet(featureVals.length);
		for (int i = 0; i < featureVals.length; i++) {
			flags.set(i);
		}
		dim = flags.cardinality();
		keys = new int[dim];
		vals = new double[dim];
		for (int i = flags.nextSetBit(0), j = 0; i >= 0; i = flags.nextSetBit(i+1), j++) {
		     keys[j] = i;
		     vals[j] = featureVals[i];
 		 }
	}

	@Override
	public String dimension(int index) {
		if (index >= dim)
			throw new IndexOutOfBoundsException(
					ErrorMessage.DIMENSION_INDEX_OUT_OF_BOUND.toString());
		return String.valueOf(keys[index]);
	}
	
	@Override
	public int dimensionValue(int index) {
		if (index >= dim)
			throw new IndexOutOfBoundsException(
					ErrorMessage.DIMENSION_INDEX_OUT_OF_BOUND.toString() 
					+ ". " + index + " vs. " + dim);
		return keys[index];
	}

	@Override
	public @Nullable Object feature(int index) {
		if (index >= dim)
			throw new IndexOutOfBoundsException(
					ErrorMessage.DIMENSION_INDEX_OUT_OF_BOUND.toString());
		return vals[index];
	}

	@Override
	public double featureValue(int index) {
		if (index >= dim)
			throw new IndexOutOfBoundsException(
					ErrorMessage.DIMENSION_INDEX_OUT_OF_BOUND.toString());
		return vals[index];
	}

	@Override
	public int size() {
		return dim;
	}

	@Override
	public int hashCode() {
		int hashcode = 0;
		for (double val : vals) {
			hashcode += (int) val;
		}
		return hashcode;
	}

	@Override
	public double featureValueAtDimension(int dimension) {
		int key = Arrays.binarySearch(this.keys, dimension);
		return (key < 0) ? Double.MIN_VALUE : vals[key];
	}

	@Override
	public Object featureAtDimension(int dimension) {
		int key = Arrays.binarySearch(this.keys, dimension);
		return (key < 0) ? Double.MIN_VALUE : vals[key];
	}
}
