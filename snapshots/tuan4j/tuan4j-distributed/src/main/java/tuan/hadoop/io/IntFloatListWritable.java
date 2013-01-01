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

/**
 * This class extends the basic java.util.List to make it writable in Hadoop
 * setting. It also supports a tuple type (integer index, float content) as
 * complex items. Such a data type is useful in many applications, e.g. 
 * managing student scores, or to rank search results.  
 * @author tuan
 *
 */
public abstract class IntFloatListWritable extends 
		ListWritable<IntFloatListWritable> {
	

	protected static final int DEFAULT_CAPACITY = 10;
	protected static final float LOAD_FACTOR = 1.5f;

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
	public abstract int[] indicesOf(float content);

	/** 
	 * Returns the index of the input element, or -1 if not found 
	 */
	public abstract int indexOf(int id);

	/** 
	 * Returns the content of the first element whose id matching
	 * the given id 
	 */
	public abstract float contentOf(int id);

	/** 
	 * Returns the last index of the input element, in case it
	 * appears many times in the array, or -1 if not found 
	 */
	public abstract int lastIndexOf(int element);

	/**
	 * Returns the index at the specified position. If the position is out of
	 * the current range of the list, the Float.MAX_VALUE will be returned
	 */
	public abstract int getIndex(int position);

	/**
	 * Returns the content at the specified position. If the position is out of
	 * the current range of the list, the Integer.MAX_VALUE will be returned
	 */
	public abstract float getContent(int position);

	/**
	 * Replaces the element at the specified position in this list with
	 * the specified element. If the position is beyond the range of the
	 * array, an IndexOutOfBoundException will be thrown.
	 *
	 * @param index index of the element to replace
	 * @param element element to be stored at the specified position
	 * @return the element previously at the specified position
	 */
	public abstract int setIndex(int position, int index);

	/**
	 * Replaces the element at the specified position in this list with
	 * the specified element. If the position is beyond the range of the
	 * array, an IndexOutOfBoundException will be thrown.
	 *
	 * @param index index of the element to replace
	 * @param element element to be stored at the specified position
	 * @return the element previously at the specified position
	 */
	public abstract float setContent(int position, float content);

	/**
	 * Removes the element at the specified position in this list. 
	 * Shifts any subsequent elements to the left (subtracts one 
	 * from their indices).
	 *
	 * @param index the index of the element to be removed
	 * @return the element that was removed from the list
	 */
	public abstract int remove(int position);
	
	/**
	 * Removes the element at the specified position in this list. 
	 * Shifts any subsequent elements to the left (subtracts one 
	 * from their indices).
	 *
	 * @param index the index of the element to be removed
	 * @return the content that was removed from the list
	 */
	public abstract float removeAndGetBackContent(int position);

	/**
	 * Appends the specified element to the end of this list.
	 */
	public abstract IntFloatListWritable add(int index, float content);

	/**
	 * Inserts the specified element at the specified position in this list.
	 * Shifts the element currently at that position (if any) and any
	 * subsequent elements to the right (adds one to their indices).
	 *
	 * @param index index at which the specified element is to be inserted
	 * @param element element to be inserted
	 */
	public abstract IntFloatListWritable add(int position, int index, 
			float content);

	/** Get the list of index in the primitive format */
	public abstract int[] indices();

	/** Get the list of content in the primitive format */
	public abstract float[] contents();

	@Override
	public void concat(ListWritable<IntFloatListWritable> lst) {
		IntFloatListWritable sortedLst = (IntFloatListWritable)lst;
		int indA = 0, indB = 0;
		
		// make sure this list is sorted upfront
		sort(true);
		
		while (indA < this.size() || indB < sortedLst.size()) {
			// if we've iterated to the end, then add from the other
			if (indA == this.size()) {
				add(sortedLst.getIndex(indB+1), sortedLst.getContent(indB+1));
				indB++;
				continue;
			} else if (indB == sortedLst.size()) {
				add(this.getIndex(indA+1), this.getContent(indA+1));
				indA++;
				continue;
			} else {
				// append the lesser value
				if (this.getIndex(indA) < sortedLst.getIndex(indB)) {
					add(this.getIndex(indA+1), this.getContent(indA+1));
					indA++;
				} else {
					add(sortedLst.getIndex(indB+1), sortedLst.getContent(indB+1));
					indB++;
				}
			}
		}
	}
	
	@Override
	public int compareTo(IntFloatListWritable other) {

		if (isEmpty()) {
			if (other.isEmpty()) {
				return 0;
			} else {
				return -1;
			}
		}

		for (int i = 0; i < size(); i++) {
			if (other.size() <= i) {
				return 1;
			}
			if (getIndex(i) < other.getIndex(i)) {
				return -1;
			} else if (getIndex(i) > other.getIndex(i)) {
				return 1;
			}
		}

		if (other.size() > size()) {
			return -1;
		} else {
			return 0;
		}
	}
	
	/**
	 * Deserializes this object.
	 *
	 * @param in source for raw byte representation
	 */
	public void readFields(DataInput in) throws IOException {
		this.clear();
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			add(i, in.readInt(), in.readFloat());
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
			out.writeInt(getIndex(i));
			out.writeFloat(getContent(i));
		}
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("[");
		for (int i = 0; i < size; i++) {
			s.append("(");
			s.append(getIndex(i));
			s.append(",");
			s.append(getContent(i));
			s.append(")");
			if (i < size - 1) {
				s.append(", ");
			}
		}
		s.append("]");
		return s.toString();
	}
}
