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

import tuan.collections.IntSet;


/**
 * This collection used BitSet to check the existence of a key
 * @author tuan
 *
 */
public class IntBitSetWritable extends IntSetWritable {

	private transient IntSet data; 
	
	@Override
	public boolean contains(int element) {
		return data.contains(element);
	}

	@Override
	public boolean containsAll(IntSetWritable subSet) {		
		int[] items = subSet.toArray();
		for (int i : items) {
			if (!contains(i)) return false;
		}
		return true;
	}

	@Override
	public boolean add(int element) {
		return data.add(element);
	}

	@Override
	public boolean addAll(IntSetWritable subSet) {
		boolean result = true;
		int[] items = subSet.toArray();
		for (int i : items) {
			result &= data.add(i);
		}
		return result;
	}

	@Override
	public boolean remove(int element) {
		return data.remove(element);
	}

	@Override
	public boolean removeAll(IntSetWritable subSet) {
		boolean result = true;
		int[] items = subSet.toArray();
		for (int i : items) {
			result &= data.remove(i);
		}
		return result;
	}

	@Override
	public boolean retainAll(IntSetWritable subSet) {
		int[] items = toArray();
		for (int i : items) {
			if (!subSet.contains(i))
				data.remove(i);
		}
		return false;
	}

	@Override
	public void ensureCapacity(int minCapacity) {
		if (data.trimToSize(minCapacity)) {
			throw new RuntimeException("hash table grows too large");
		}
	}

	@Override
	public void trimToSize() {
		data.trim();
	}

	@Override
	public void clear() {
		data.clear();		
	}	

	@Override
	public int[] toArray() {		
		return data.toArray();
	}

}
