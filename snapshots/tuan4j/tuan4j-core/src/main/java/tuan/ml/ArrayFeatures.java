package tuan.ml;

import java.util.Arrays;
import java.util.BitSet;

/**
 * This is the basic implementation of Features where data are stored in 
 * primitive arrays. It sorts the dimension ids so that the access to
 * feature values at a specific dimension can be done in O(logn), where
 * n is a number of dimensions with positive feature values of a document
 */
public class ArrayFeatures implements RandomAccessFeatures {

	/** the number of active dimensions */
	private int dim;

	/** the current mapped dimensions. Useful when the object
	 * is instantiated with the dummy constructor */
	private int mappedDim = Integer.MIN_VALUE;
	
	/** We use contiguous arrays to store features' values */
	
	// this stores id of dimensions. It has to be sorted in ascendant order
	private int keys[];
	
	// we need to sort the local index after all features are populated and
	// mapped. This flag is to check whether the index is sorted or not, and
	// it will silently sort it the first time one feature is accessed
	private boolean sorted;
	
	// this stores values at a specific dimension. The value Double.MIN_VALUE
	// indicates the absence of features at a dimension (over-fitting issue),
	private double vals[];
	
	private String cachedString;
	
	/**
	 * This is a dummy constructor with unmapped local-global dimensions and
	 * zero values of all features
	 */
	public ArrayFeatures(int dim) {
		if (dim == 0) 
			throw new NullPointerException(ErrorMessage.FEATURES_NULL.toString());
		this.dim = dim;
		mappedDim = 0;
		keys = new int[dim];
		vals = new double[dim];
	}
	
	/** 
	 * This is probably the most popular constructor for array-based features. The
	 * best practice is to sort dimension ids before instantiating the feature object.
	 * For the sake of memory usage and to maintain the immutability of programs, all
	 * arrays are copied and compacted to dim size (it implicitly assumes the dimension
	 * does not change subsequently)
	 */
	public ArrayFeatures(int[] keys, double[] vals, int dim) {
		if (keys == null) 
			throw new NullPointerException(ErrorMessage.DOC_KEY_NULL.toString());
		if (vals == null) 
			throw new NullPointerException(ErrorMessage.FEATURES_NULL.toString());
		this.keys = Arrays.copyOf(keys, dim);
		this.vals = Arrays.copyOf(vals, dim);
		this.dim = dim;
		this.mappedDim = dim;
		this.sorted = true;
	}
	
	/** 
	 * This is the quick constructor for array-based features. It follows the same
	 * best behaviors and develops the same best practices as 
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
		this.mappedDim = this.dim;
		this.sorted = true;
	}
	
	public ArrayFeatures(double... featureVals) {
		if (featureVals == null)
			throw new NullPointerException(ErrorMessage.FEATURES_NULL.toString());
		BitSet flags = new BitSet(featureVals.length);
		for (int i = 0; i < featureVals.length; i++) {
			flags.set(i);
		}
		dim = flags.cardinality();
		mappedDim = dim;
		keys = new int[dim];
		vals = new double[dim];
		for (int i = flags.nextSetBit(0), j = 0; i >= 0; i = flags.nextSetBit(i+1), j++) {
		     keys[j] = i;
		     vals[j] = featureVals[i];
 		 }
		sorted = true;
	}

	@Override
	public String dimension(int index) {
		if (mappedDim != dim)
			throw new RuntimeException(
				ErrorMessage.FEATURES_UNMAPPED.toString());
		if (index >= dim) 			
			 throw new IndexOutOfBoundsException(
					ErrorMessage.DIMENSION_INDEX_OUT_OF_BOUND.toString() 
					+ ". " + index + " vs. " + dim);
		if (!sorted) sort();
		return String.valueOf(keys[index]);
	}
	
	@Override
	public int dimensionValue(int index) {
		if (mappedDim != dim)
			throw new RuntimeException(
				ErrorMessage.FEATURES_UNMAPPED.toString());
		if (index >= dim) 			
			 throw new IndexOutOfBoundsException(
					ErrorMessage.DIMENSION_INDEX_OUT_OF_BOUND.toString() 
					+ ". " + index + " vs. " + dim);
		if (!sorted) sort();
		return keys[index];
	}

	@Override
	public Object feature(int index) {
		if (mappedDim != dim)
			throw new RuntimeException(
				ErrorMessage.FEATURES_UNMAPPED.toString());
		if (index >= dim) 			
			 throw new IndexOutOfBoundsException(
					ErrorMessage.DIMENSION_INDEX_OUT_OF_BOUND.toString() 
					+ ". " + index + " vs. " + dim);
		if (!sorted) sort();
		return vals[index];
	}

	@Override
	public double featureValue(int index) {
		if (mappedDim != dim)
			throw new RuntimeException(
				ErrorMessage.FEATURES_UNMAPPED.toString());
		if (index >= dim) 			
			 throw new IndexOutOfBoundsException(
					ErrorMessage.DIMENSION_INDEX_OUT_OF_BOUND.toString() 
					+ ". " + index + " vs. " + dim);
		if (!sorted) sort();
		return vals[index];
	}

	@Override
	public int size() {
		return dim;
	}

	@Override
	public int hashCode() {
		if (mappedDim != dim)
			throw new RuntimeException(
				ErrorMessage.FEATURES_UNMAPPED.toString());
		int hashcode = 0;
		for (double val : vals) {
			hashcode += (int) val;
		}
		return hashcode;
	}
	
	@Override
	public String toString() {
		if (mappedDim != dim)
			throw new RuntimeException(
				ErrorMessage.FEATURES_UNMAPPED.toString());
		if (cachedString == null) {
			StringBuilder sb = new StringBuilder();
			if (!sorted) sort();
			for (int i = 0; i < dim; i++) {
				sb.append(keys[i]).append("\t").append(vals[i]).append("\t");
			}
			cachedString = sb.toString();
		}
		return cachedString;
	}

	@Override
	public double featureValueAtDimension(int dimension) {
		if (!sorted) sort();
		int key = Arrays.binarySearch(this.keys, dimension);
		return (key < 0) ? Double.MIN_VALUE : vals[key];
	}

	@Override
	public Object featureAtDimension(int dimension) throws UnsupportedOperationException {
		if (!sorted) sort();
		int key = Arrays.binarySearch(this.keys, dimension);
		return (key < 0) ? Double.MIN_VALUE : vals[key];
	}
	
	@Override
	public void updateFeature(int dimension, double value) {
		if (!sorted) sort();
		int key = Arrays.binarySearch(this.keys, dimension);
		if (key >= 0) vals[key] = value;
		else throw new IndexOutOfBoundsException(
				ErrorMessage.DIMENSION_INDEX_OUT_OF_BOUND.toString() 
				+ ":" + dim); 
		
	}
	
	@Override
	public void updateLocalFeature(int index, double value) {
		if (index >= dim) 			
			 throw new IndexOutOfBoundsException(
					ErrorMessage.DIMENSION_INDEX_OUT_OF_BOUND.toString() 
					+ ". " + index + " vs. " + dim);
		vals[index] = value;		
	}

	@Override
	public void addFeatures(int dim, double value)
			throws UnsupportedOperationException {		
		if (mappedDim == dim)
			throw new IndexOutOfBoundsException(
					ErrorMessage.DIMENSION_MAPPED.toString());
		else {
			keys[mappedDim] = dim;
			vals[mappedDim++] = value;
		}
	}
	
	// A pay-as-you-go method that silently re-sort the local index and 
	// feature arrays the first time one access method is called
	private void sort() {
		
		// check if re-sort is needed (to avoid one unnecessary array copy)
		// Arrays.
	}
}
