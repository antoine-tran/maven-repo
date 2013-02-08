package tuan.collections;

import java.util.Arrays;

/**
 * This code is redistributed from the project Kryo under license Apache 2.0 (below)
 * All copyrights reserved.
 * My big thanks to Nathan Sweet
 * 
 * More details are found at: http://kryo.googlecode.com
 * 
 *******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************
 *
 * A resizable, ordered or unordered int array. Avoids the boxing that occurs with ArrayList<Integer>. If unordered, this class
 * avoids a memory copy when removing elements (the last element is moved to the removed element's position).
 * @author Nathan Sweet */

public class IntArrayList {
	public int[] items;
	public int size;
	public boolean ordered;

	/** Creates an ordered array with a capacity of 16. */
	public IntArrayList () {
		this(true, 16);
	}

	/** Creates an ordered array with the specified capacity. */
	public IntArrayList (int capacity) {
		this(true, capacity);
	}

	/** @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
	 *           memory copy.
	 * @param capacity Any elements added beyond this will cause the backing array to be grown. */
	public IntArrayList (boolean ordered, int capacity) {
		this.ordered = ordered;
		items = new int[capacity];
	}

	/** Creates a new array containing the elements in the specific array. The new array will be ordered if the specific array is
	 * ordered. The capacity is set to the number of elements, so any subsequent elements added will cause the backing array to be
	 * grown. */
	public IntArrayList (IntArrayList array) {
		this.ordered = array.ordered;
		size = array.size;
		items = new int[size];
		System.arraycopy(array.items, 0, items, 0, size);
	}

	/** Creates a new ordered array containing the elements in the specified array. The capacity is set to the number of elements,
	 * so any subsequent elements added will cause the backing array to be grown. */
	public IntArrayList (int[] array) {
		this(true, array);
	}

	/** Creates a new array containing the elements in the specified array. The capacity is set to the number of elements, so any
	 * subsequent elements added will cause the backing array to be grown.
	 * @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
	 *           memory copy. */
	public IntArrayList (boolean ordered, int[] array) {
		this(ordered, array.length);
		size = array.length;
		System.arraycopy(array, 0, items, 0, size);
	}

	public void add (int value) {
		int[] items = this.items;
		if (size == items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
		items[size++] = value;
	}

	public void addAll (IntArrayList array) {
		addAll(array, 0, array.size);
	}

	public void addAll (IntArrayList array, int offset, int length) {
		if (offset + length > array.size)
			throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
		addAll(array.items, offset, length);
	}

	public void addAll (int[] array) {
		addAll(array, 0, array.length);
	}

	public void addAll (int[] array, int offset, int length) {
		int[] items = this.items;
		int sizeNeeded = size + length - offset;
		if (sizeNeeded >= items.length) items = resize(Math.max(8, (int)(sizeNeeded * 1.75f)));
		System.arraycopy(array, offset, items, size, length);
		size += length;
	}

	public int get (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		return items[index];
	}

	public void set (int index, int value) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		items[index] = value;
	}

	public void insert (int index, int value) {
		int[] items = this.items;
		if (size == items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
		if (ordered)
			System.arraycopy(items, index, items, index + 1, size - index);
		else
			items[size] = items[index];
		size++;
		items[index] = value;
	}

	public void swap (int first, int second) {
		if (first >= size) throw new IndexOutOfBoundsException(String.valueOf(first));
		if (second >= size) throw new IndexOutOfBoundsException(String.valueOf(second));
		int[] items = this.items;
		int firstValue = items[first];
		items[first] = items[second];
		items[second] = firstValue;
	}

	public boolean contains (int value) {
		int i = size - 1;
		int[] items = this.items;
		while (i >= 0)
			if (items[i--] == value) return true;
		return false;
	}

	public int indexOf (int value) {
		int[] items = this.items;
		for (int i = 0, n = size; i < n; i++)
			if (items[i] == value) return i;
		return -1;
	}

	public boolean removeValue (int value) {
		int[] items = this.items;
		for (int i = 0, n = size; i < n; i++) {
			if (items[i] == value) {
				removeIndex(i);
				return true;
			}
		}
		return false;
	}

	/** Removes and returns the item at the specified index. */
	public int removeIndex (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		int[] items = this.items;
		int value = items[index];
		size--;
		if (ordered)
			System.arraycopy(items, index + 1, items, index, size - index);
		else
			items[index] = items[size];
		return value;
	}

	/** Removes and returns the last item. */
	public int pop () {
		return items[--size];
	}

	/** Returns the last item. */
	public int peek () {
		return items[size - 1];
	}

	public void clear () {
		size = 0;
	}
	
	public int size() {
		return size;
	}

	/** Reduces the size of the backing array to the size of the actual items. This is useful to release memory when many items have
	 * been removed, or if it is known that more items will not be added. */
	public void shrink () {
		resize(size);
	}

	/** Increases the size of the backing array to acommodate the specified number of additional items. Useful before adding many
	 * items to avoid multiple backing array resizes.
	 * @return {@link #items} */
	public int[] ensureCapacity (int additionalCapacity) {
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= items.length) resize(Math.max(8, sizeNeeded));
		return items;
	}

	protected int[] resize (int newSize) {
		int[] newItems = new int[newSize];
		int[] items = this.items;
		System.arraycopy(items, 0, newItems, 0, Math.min(items.length, newItems.length));
		this.items = newItems;
		return newItems;
	}

	public void sort () {
		Arrays.sort(items, 0, size);
	}

	public void reverse () {
		for (int i = 0, lastIndex = size - 1, n = size / 2; i < n; i++) {
			int ii = lastIndex - i;
			int temp = items[i];
			items[i] = items[ii];
			items[ii] = temp;
		}
	}

	/** Reduces the size of the array to the specified size. If the array is already smaller than the specified size, no action is
	 * taken. */
	 public void truncate (int newSize) {
		if (size > newSize) size = newSize;
	}

	public int[] toArray () {
		int[] array = new int[size];
		System.arraycopy(items, 0, array, 0, size);
		return array;
	}

	@Override
	public String toString () {
		if (size == 0) return "[]";
		int[] items = this.items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		buffer.append(items[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(", ");
			buffer.append(items[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}

	public String toString (String separator) {
		if (size == 0) return "";
		int[] items = this.items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append(items[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(separator);
			buffer.append(items[i]);
		}
		return buffer.toString();
	}
	
	public static int intersect(IntArrayList list1, IntArrayList list2) {
		if (list1 == null || list2 == null)
			return 0;
		list1.sort();
		list2.sort();
		int intersect = 0;
		for (int i = 0; i < list1.size; i++) {
			if (list2.indexOf(list1.get(i)) >= 0)
				++intersect;
		}
		return intersect;
	}

	/**
	 * Return the list of common values between two sets
	 * @param list1 the list of distinctive integers
	 * @param list2the list of distinctive integers
	 * @return the number of common integers
	 */
	public static IntArrayList intersectList(IntArrayList list1, IntArrayList list2) {
		IntArrayList resLst = null;
		if (list1 != null && list2 != null) {
			resLst = new IntArrayList();
			list1.sort();
			list2.sort();
			for (int i = 0; i < list1.size; i++) {
				if (list2.indexOf(list1.get(i)) >= 0)
					resLst.add(list1.get(i));
			}	
		}					
		return resLst;
	}

	/**
	 * Return the number of common values between an arbitrary number of  sets
	 * @return the number of common integers, -1 if the argument is invalid
	 */
	public static int intersect(IntArrayList... list) {
		int intersect = 0;
		int n = list.length;
		if (n <= 1) return -1;
		for (IntArrayList listElem : list)
			listElem.sort();
		IntArrayList arr1 = list[0];
		boolean found;		
		for (int i = 0; i < arr1.size; i++) {
			found = true;
			for (int j = 1; j < n; j++)  
				if (list[j].indexOf(arr1.get(i)) >= 0) {
					found = false;
					break;
				}
			if (found) ++intersect;

		}
		return intersect;
	}

	/**
	 * Return the dice's similarity between an arbitrary number of  sets 
	 * @param the double value of dice's similarity
	 */
	public static double dice(IntArrayList... list) {
		int n = list.length;
		int sum = 0;
		for (IntArrayList listElem : list)
			sum += listElem.size();
		return (double)(n * intersect(list)) / (double)sum; 
	}

	/**
	 * Return the extended dice's similarity between an arbitrary number of sets
	 * with specified intersection size
	 * @param intersect the size of the sets' intersection
	 * @param list list of sets
	 * @return
	 */
	public static double dice(int intersect, IntArrayList... list) {
		int n = list.length;
		int sum = 0;
		for (IntArrayList listElem : list)
			sum += listElem.size();
		return (double)(n * intersect) / (double)sum; 
	}
}
