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
 * @author tuan
 *
 */
public abstract class IntSetWritable extends SetWritable<IntSetWritable> {

	public abstract boolean contains(int element);
	
	public abstract boolean containsAll(IntSetWritable subSet);
	
	public abstract boolean add(int element);
	
	public abstract boolean addAll(IntSetWritable subSet);
	
	public abstract boolean remove(int element);
	
	public abstract boolean removeAll(IntSetWritable subSet);
	
	public abstract boolean retainAll(IntSetWritable subSet);
	
	public abstract int[] toArray();
	
	@Override
	/**
	 * This is counter-intuitive when one tries to compare two sets. 
	 * However, since this is necessary in Hadoop, we will provide a naive
	 * measure based on hashcodes of the elements
	 * 
	 */
	public int compareTo(IntSetWritable set2) {
		int[] theseItems = toArray();
		int[] thoseItems = set2.toArray();
		int thisHashcode = 0, thatHashcode = 0;
		for (int i : theseItems) thisHashcode += i;
		for (int i : thoseItems) thatHashcode += i;
		if (thisHashcode > thatHashcode) return 1;
		else if (thisHashcode == thatHashcode) return 0;
		else return -1;
	}
	
	/**
	 * Deserializes this object.
	 *
	 * @param in source for raw byte representation
	 */
	public void readFields(DataInput in) throws IOException {
		clear();
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			add(in.readInt());
		}
	}

	/**
	 * Serializes this object.
	 *
	 * @param out	where to write the raw byte representation
	 */
	public void write(DataOutput out) throws IOException {		
		int[] elems = toArray();
		out.writeInt(elems.length);
		for (int i = 0; i < elems.length; i++) {
			out.writeInt(elems[i]);
		}
	}
	
	@Override
	public String toString() {
		int[] elems = toArray();
		StringBuilder s = new StringBuilder("[");
		for (int i = 0; i < size; i++) {
			s.append(elems[i]);
			if (i < size - 1) {
				s.append(", ");
			}
		}
		s.append("]");
		return s.toString();
	}
}
