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
package tuan.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Extended version of ArrayList that support push operator methods in
 * asynchronous applications. It wraps basic Java ArrayList class, but
 * only limits access to reading and adding new items. No modifying operation
 * (removal, sorting, etc.) is allowed. Because ArrayList is not thread-safe,
 * PushArrayList is not thread-safe either. 
 * <p>PushArrayList supports partially random access to items as compared with
 * ArrayList. More specifically, it provides random access to only items that
 * are read over (items that are fetched via next() method).</p>
 * <p>Like a typical push operator, a PushArrayList object needs to be
 * activated with open() method before any other operations. A de-activation
 * operation is done by calling close() method, and after this, all access to
 * the object is not permitted.</p>
 * 
 * @author tuan
 * @version 0.0.1
 * @since 2012-02-01
 *
 */
public class PushArrayList<T> implements PushIterator<T> {

	// Underlying data array. This supports instance random access
	private ArrayList<T> arrayList;

	// the current cursor position. Negative cursor value means the object is
	// not activated yet, or the ArrayList object is currently empty
	private int cursor = -1;

	// the cursor pointing to the last 'stable' item. This is a 'virtual' size
	// indicator of the ArrayList object. Negative cursor value means the
	// object is not activated yet, or the ArrayList object is currently
	// empty
	private int capacity = -1;

	@Override
	public void open() {
		arrayList = new ArrayList<T>();
	}

	@Override
	public void close() {
		arrayList = null; 	//enable garbage collection
		capacity = -1;
		cursor = -1;
	}

	@Override
	public boolean hasNext() {
		return (cursor >= 0 && cursor < capacity);
	}

	@Override
	public T next() {
		if (capacity < 0)
			throw new NoSuchElementException(
					"The iterator is not yet activated"); 
		cursor++;
		return arrayList.get(cursor);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"remove() is not supported in push operators");
	}

	@Override
	public void add(T item) {
		if (arrayList == null)
			throw new IllegalStateException(
					"The iterator is not yet activated");
		
		// TODO: Add null item is permitted
		arrayList.add(item);
		capacity++;
	}

	@Override
	public void addAll(Iterator<T> items) {
		if (arrayList == null)
			throw new IllegalStateException(
					"The iterator is not yet activated");
		int i = 0;
		// TODO: Add rollback operation when things go wrong
		// (i.e., revert the ArrayList to the last stable object
		while (items.hasNext()) {
			arrayList.add(items.next());
			i++;
		}
		capacity += i;
	}
	
	public int size() {
		if (arrayList == null)
			throw new IllegalStateException(
					"The iterator is not yet activated");
		return capacity + 1;
	}
	
	/** 
	 * Return the element at a given index. If the element is
	 * not visited, NoSuchElementException is thrown. If the
	 * index is negative or greater than the current capacity, 
	 * an IndexOutOfBoundsException is thrown 
	 */
	public T get(int index) {
		if (arrayList == null)
			throw new IllegalStateException(
					"The iterator is not yet activated");
		if (index > capacity)
			throw new IndexOutOfBoundsException(
					"index out of range: " + index);
		else if (index > cursor)
			throw new NoSuchElementException(
					"Cannot access elements that are not fetched");
		else return arrayList.get(index);
	}
}
