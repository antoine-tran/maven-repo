/**
 * ==================================
 * 
 * Copyright (c) 2010 Anh Tuan Tran
 *
 * URL: http://www.mpi-inf.mpg.de/~attran,
 *          http://www.l3s.de/~ttran
 *
 * Email: tranatuan24@gmail.com
 * ==================================
 * 
 * This source code is provided with AS IF - it does not guarantee the
 * or compatibilities with older or newer version of third-parties. In any
 * cases, if you have problems regarding using libraries delivered with
 * the project, feel free to write to the above email. Also, we would like
 * to get feedbacks from all of you
 */
package tuan.hadoop.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.RandomAccess;
import org.apache.hadoop.io.WritableComparable;

/**
 * This class extends the Java ArrayList to be writable in Hadoop setting.
 * It is an abstract extension, i.e. no data structure for the 
 * array is specified. It has to be implemented further in order to be used    
 * @author tuan
 *
 */ 
public abstract class IntArrayListWritable implements RandomAccess, 
		WritableComparable<IntArrayListWritable> {

	/** real number of elements */
	protected transient int size;
	
	/** initial capacity of the array by default */
	protected static final int INITIAL_CAPACITY_DEFAULT = 10;

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return (size == 0);
	}

	/**
	 * Specifies the length of this list.
	 */
	public void setSize(int size) {
		ensureCapacity(size);
		this.size = size;
	}

	/**
	 * Increases the capacity of this object, if necessary, to ensure that 
	 * it can hold at least the number of elements specified by the minimum
	 * capacity argument. 
	 * 
	 * @param minCapacity the desired minimum capacity
	 */
	public abstract void ensureCapacity(int minCapacity);

	/**
	 * Trims the capacity of this object to be the list's current size.
	 * An application can use this operation to minimize the memory 
	 * footprint of the object.
	 */
	public abstract void trimToSize();

	/**
	 * Returns <tt>true</tt> if this list contains the input element.
	 *
	 * @param n element under consideration
	 * @return <tt>true</tt> if this list contains the input element
	 */
	public boolean contains(int element) {
		return (indexOf(element) >= 0);
	}
	
	/** 
	 * Returns the index of the input element, or -1 if not found 
	 */
	public abstract int indexOf(int element);
	
	/** 
	 * Returns the last index of the input element, in case it
	 * appears many times in the array, or -1 if not found 
	 */
	public abstract int lastIndexOf(int element);
	
	/**
	 * Returns the element at the specified position
	 */
	public abstract int get(int index);
	
	/**
     * Replaces the element at the specified position in this list with
     * the specified element. If the position is beyond the range of the
     * array, nothing happens.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     */
    public abstract int set(int index, int element);
	
    /**
     * Removes the element at the specified position in this list. 
     * Shifts any subsequent elements to the left (subtracts one 
     * from their indices).
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     */
    public abstract int remove(int index);
    
    /**
     * Appends the specified element to the end of this list.
     */
    public abstract IntArrayListWritable add(int e);

    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     */
    public abstract IntArrayListWritable add(int index, int element);
    
	/**
	 * Stability sort the elements of the array
	 */
	public abstract void sort(boolean ascending);
	
	/**
	 * Removes all of the elements from this list. The list will
	 * be empty after this call returns.
	 */
	public abstract void clear();
	
	/** Get the data in the primitive format */
	public abstract int[] toArray();
	
	
	/** 
	 * Appends data of the other {@link IntArrayListWritable} into the tail of
	 * the array */
	public abstract IntArrayListWritable concat(IntArrayListWritable obj);
	
	/**
	   * Deserializes this object.
	   *
	   * @param in source for raw byte representation
	   */
	  public void readFields(DataInput in) throws IOException {
	    this.clear();
	    int size = in.readInt();
	    for (int i = 0; i < size; i++) {
	      add(i, in.readInt());
	    }
	  }

	  /**
	   * Serializes this object.
	   *
	   * @param out	where to write the raw byte representation
	   */
	  public void write(DataOutput out) throws IOException {
	    int size = size();
	    out.writeInt(size);
	    for (int i = 0; i < size; i++) {
	      out.writeInt(get(i));
	    }
	  }
	
	/** All implementations have to give an explicit representation of the array */
	public abstract String toString();
}
