package tuan.hadoop.io;

import java.util.Arrays;


/**
 * This class tailors the two classes ArrayListOfInts and 
 * ArrayListOfIntsWritable of Jimmy Lin's Cloud9 Map-Reduce
 * librarry (http://lintool.github.com/Cloud9), to make it
 * more flexible to other frameworks.
 * 
 * @author tuan
 *
 */
public class IntArrayListWritable extends IntListWritable {

	private transient int[] array;

	/**
	 * Constructs an empty list with the specified initial capacity.
	 *
	 * @param initialCapacity the initial capacity of the list
	 */
	public IntArrayListWritable(int initCapacity) {
		if (initCapacity < 0) {
			throw new IllegalArgumentException(
					"Illegal Capacity: " + initCapacity);
		}

		array = new int[initCapacity];
	}

	/**
	 * Constructs an empty list with an initial capacity of ten.
	 */
	public IntArrayListWritable() {
		this(INITIAL_CAPACITY_DEFAULT);
	}

	/**
	 * Constructs a list from an array. Defensively makes a copy of the array.
	 *
	 * @param a source array
	 */
	public IntArrayListWritable(int[] a) {
		if (a != null) {
			// Be defensive and make a copy of the array.
			array = Arrays.copyOf(a, a.length);
			size = array.length;	
		}		
	}

	/**
	 * Constructs a list populated with ints in range [first, last).
	 *
	 * @param first the smallest int in the range (inclusive)
	 * @param last the largest int in the range (exclusive)
	 */
	public IntArrayListWritable(int first, int last) {
		this(last - first);

		int j = 0;
		for (int i = first; i < last; i++) {
			this.add(j++, i);
		}
	}

	@Override
	public void ensureCapacity(int minCapacity) {
		int oldCapacity = array.length;
		if (minCapacity > oldCapacity) {
			int newCapacity = (oldCapacity * 3) / 2 + 1;
			if (newCapacity < minCapacity) {
				newCapacity = minCapacity;
			}
			array = Arrays.copyOf(array, newCapacity);
		}
	}

	@Override
	public void trimToSize() {
		int oldCapacity = array.length;
		if (size < oldCapacity) {
			array = Arrays.copyOf(array, size);
		}
	}

	@Override
	public int indexOf(int element) {
		for (int i = 0; i < size; i++) {
			if (element == array[i]) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(int element) {
		for (int i = size - 1; i >= 0; i--) {
			if (element == array[i]) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int get(int index) {
		return array[index];
	}

	@Override
	public int set(int index, int element) {
		int oldValue = array[index];
		array[index] = element;
		return oldValue;
	}

	@Override
	public int remove(int index) {
		if (index >= size) {
			throw new ArrayIndexOutOfBoundsException();
		}
		int oldValue = array[index];

		int numMoved = size - index - 1;
		if (numMoved > 0) {
			System.arraycopy(array, index + 1, array, index, numMoved);
		}

		size--;
		return oldValue;
	}

	@Override
	public IntArrayListWritable add(int e) {
		ensureCapacity(size + 1); // Increments modCount!!
		array[size++] = e;
		return this;
	}

	@Override
	public IntArrayListWritable add(int index, int element) {
		if (index > size || index < 0) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
		}

		ensureCapacity(size + 1); // Increments modCount!!
		System.arraycopy(array, index, array, index + 1, size - index);
		array[index] = element;
		size++;
		return this;
	}

	@Override
	public void sort(boolean ascending) {
		trimToSize();
		if (ascending) {
			Arrays.sort(array);	
		}
		else {
			int[] tmpArr = new int[size];
			int j = 0;			
			for (int i = array.length-1; i >= 0; i--) {
				tmpArr[ j++ ] = array[i];
			}
			Arrays.sort(tmpArr);	
			array = tmpArr;
		}
	}

	@Override
	public void clear() {
		size = 0;
		array = new int[INITIAL_CAPACITY_DEFAULT];
	}

	@Override
	public int[] toArray() {
		return array;
	}
}
