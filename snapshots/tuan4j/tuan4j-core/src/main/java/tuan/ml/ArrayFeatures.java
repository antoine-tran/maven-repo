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
	public void update(int dimension, double value) {
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
	public void add(int dim, double value)
			throws UnsupportedOperationException {		
		if (mappedDim == dim)
			throw new IndexOutOfBoundsException(
					ErrorMessage.DIMENSION_MAPPED.toString());
		else {
			keys[mappedDim] = dim;
			vals[mappedDim++] = value;
		}
	}
	

	@Override
	// TODO: ArrayFeatures does not support nominal features, so we simply take it around.
	// This might a specs hole
	public void update(int dim, Object value) {
		update(dim, value.hashCode());
	}

	@Override
	// TODO: ArrayFeatures does not support nominal features, so we simply take it around.
	// This might a specs hole	
	public void add(int dim, Object value) throws UnsupportedOperationException {
		add(dim, value.hashCode());
	}

	@Override
	// TODO: ArrayFeatures does not support nominal features, so we simply take it around.
	// This might a specs hole
	public void updateLocalFeature(int index, Object value) {
		updateLocalFeature(dim, value.hashCode());		
	}
	
	// A pay-as-you-go method that silently re-sort the local index and 
	// feature arrays the first time one access method is called
	private void sort() {
		
		// check if re-sort is needed (to avoid one unnecessary array copy)
		// Arrays.		
		boolean ascending = true;
		if (dim <= 1) return;
		for (int i = 1; i < keys.length && ascending; i++) {
			ascending &= (keys[i] >= keys[i-1]);
		}
		if (!ascending) {
			sort(keys, vals, 0, dim);
		}
	}
	
	// The following code snippets are copied from Java openJDK 6-b14 source code,
	// with some adaptations to make it able to sort two arrays in parallel
	/**
     * Sorts the specified sub-array of integers into ascending order.
     */
    private static void sort(int x[], double[] y, int off, int len) {
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (int i=off; i<len+off; i++)
                for (int j=i; j>off && x[j-1]>x[j]; j--)
                    swap(x, y, j, j-1);
            return;
        }

        // Choose a partition element, v
        int m = off + (len >> 1);       // Small arrays, middle element
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {        // Big arrays, pseudomedian of 9
                int s = len/8;
                l = med3(x, l,     l+s, l+2*s);
                m = med3(x, m-s,   m,   m+s);
                n = med3(x, n-2*s, n-s, n);
            }
            m = med3(x, l, m, n); // Mid-size, med of 3
        }
        int v = x[m];

        // Establish Invariant: v* (<v)* (>v)* v*
        int a = off, b = a, c = off + len - 1, d = c;
        while(true) {
            while (b <= c && x[b] <= v) {
                if (x[b] == v)
                    swap(x, y, a++, b);
                b++;
            }
            while (c >= b && x[c] >= v) {
                if (x[c] == v)
                    swap(x, y, c, d--);
                c--;
            }
            if (b > c)
                break;
            swap(x, y, b++, c--);
        }

        // Swap partition elements back to middle
        int s, n = off + len;
        s = Math.min(a-off, b-a  );  vecswap(x, y, off, b-s, s);
        s = Math.min(d-c,   n-d-1);  vecswap(x, y, b,   n-s, s);

        // Recursively sort non-partition-elements
        if ((s = b-a) > 1)
            sort(x, y, off, s);
        if ((s = d-c) > 1)
            sort(x, y, n-s, s);
    }

    /**
     * Swaps x[a] with x[b].
     */
    private static void swap(int x[], double[] y, int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
        double s = y[a];
        y[a] = y[b];
        y[b] = s;
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(int x[], double[] y, int a, int b, int n) {
        for (int i=0; i<n; i++, a++, b++)
            swap(x, y, a, b);
    }

    /**
     * Returns the index of the median of the three indexed integers.
     */
    private static int med3(int x[], int a, int b, int c) {
        return (x[a] < x[b] ?
                (x[b] < x[c] ? b : x[a] < x[c] ? c : a) :
                (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
    }
}
