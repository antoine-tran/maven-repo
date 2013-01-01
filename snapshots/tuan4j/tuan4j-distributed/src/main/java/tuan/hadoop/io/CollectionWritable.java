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

import org.apache.hadoop.io.WritableComparable;

/**
 * @author tuan
 * @param <T>
 *
 */
public abstract class CollectionWritable<T> implements WritableComparable<T> {

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
	 * Removes all of the elements from this list. The list will
	 * be empty after this call returns.
	 */
	public abstract void clear();
	
	/**
	   * Deserializes this object.
	   *
	   * @param in source for raw byte representation
	   */
	  public abstract void readFields(DataInput in) throws IOException;

	  /**
	   * Serializes this object.
	   *
	   * @param out	where to write the raw byte representation
	   */
	  public abstract void write(DataOutput out) throws IOException;
	
	/** All implementations have to give an explicit representation of the array */
	public abstract String toString();
}
