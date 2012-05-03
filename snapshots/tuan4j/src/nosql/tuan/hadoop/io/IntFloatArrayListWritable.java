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

import java.util.Arrays;

import tuan.core.NotImplementedException;

/**
 * A simple and not-so-memory-efficient implementation of IntFloatListWritable
 * using two synchronized primitive arrays. It takes O(n) time for most of the
 * insert / delete / update operations though....
 * @author tuan
 *
 */
public class IntFloatArrayListWritable extends IntFloatListWritable {

	private int[] indices;
	private float[] contents;

	// current size of the arrays
	private int size;

	public IntFloatArrayListWritable() {
		indices = new int[DEFAULT_CAPACITY];
		contents = new float[DEFAULT_CAPACITY];
	}

	public IntFloatArrayListWritable(int size) {
		indices = new int[size];
		contents = new float[size];
	}


	@Override
	public int indexOf(int id) {
		for (int i = 0; i < size; i++) {
			if (indices[i] == id)
				return i;
		}
		return -1;
	}

	@Override
	public float contentOf(int id) {
		for (int i = 0; i < size; i++) {
			if (indices[i] == id)
				return contents[i];
		}
		return -1;
	}

	@Override
	public int lastIndexOf(int element) {
		for (int i = size - 1; i >= 0; i--) {
			if (indices[i] == element)
				return i;
		}		
		return -1;
	}

	@Override
	public int getIndex(int position) {	
		if (position < 0 || position >= size)
			return Integer.MAX_VALUE;
		return indices[position];
	}

	@Override
	public float getContent(int position) {
		if (position < 0 || position >= size)
			return Float.MAX_VALUE;
		return contents[position];
	}

	@Override
	public int setIndex(int position, int index) {
		if (position < 0 || position >= size)
			throw new IndexOutOfBoundsException();
		else {
			int oldIndex = indices[position];
			indices[position] = index;
			return oldIndex;
		}
	}

	@Override
	public float setContent(int position, float content) {
		if (position < 0 || position >= size)
			throw new IndexOutOfBoundsException();
		float oldContent = contents[position];
		contents[position] = content;
		return oldContent;
	}

	@Override
	public int remove(int position) {
		if (position < 0 || position >= size)
			throw new IndexOutOfBoundsException();
		int oldIndex = indices[position];
		int movedItemNo = size - position - 1;
		if (movedItemNo > 0) {
			System.arraycopy(indices, position + 1, indices, position, 
					movedItemNo);
			System.arraycopy(contents, position + 1, contents, position, 
					movedItemNo);
		}
		size--;
		return oldIndex;
	}
	
	@Override
	public float removeAndGetBackContent(int position) {
		if (position < 0 || position >= size)
			throw new IndexOutOfBoundsException();
		float oldValue = contents[position];
		int movedItemNo = size - position - 1;
		if (movedItemNo > 0) {
			System.arraycopy(indices, position + 1, indices, position, 
					movedItemNo);
			System.arraycopy(contents, position + 1, contents, position, 
					movedItemNo);
		}
		size--;
		return oldValue;
	}
	

	@Override
	public IntFloatArrayListWritable add(int index, float content) {
		ensureCapacity(size + 1);
		indices[size + 1] = index;
		contents[size + 1] = content;
		size++;
		return this;
	}

	@Override
	public IntFloatArrayListWritable add(int position, int index, float content) {
		if (position < 0 || position >= size)
			throw new IndexOutOfBoundsException();
		ensureCapacity(size + 1);
		System.arraycopy(indices, position, indices, position + 1, 
				size - position - 1);
		System.arraycopy(contents, position, contents, position + 1, 
				size - position);
		indices[position] = index;
		contents[position] = content;
		size++;
		return this;
	}

	@Override
	public int[] indices() {
		return indices;
	}

	@Override
	public float[] contents() {
		return contents;
	}

	@Override
	public void ensureCapacity(int minCapacity) {
		if (minCapacity > indices.length) {
			int newCapacity = (int) (minCapacity * LOAD_FACTOR);
			indices = Arrays.copyOf(indices, newCapacity);
			contents = Arrays.copyOf(contents, newCapacity);
		}
	}

	@Override
	public void trimToSize() {
		if (size < indices.length) {
			indices = Arrays.copyOf(indices, size);
			contents = Arrays.copyOf(contents, size);
		}		
	}

	@Override
	public void sort(boolean ascending) {
		trimToSize();
		Arrays.sort(indices);
		Arrays.sort(contents);
		if (!ascending) {
			int[] tmpIndices = new int[size];
			float[] tmpContents = new float[size];
			int j = 0;			
			for (int i = size; i >= 0; i--) {
				tmpIndices[j] = indices[i];
				tmpContents[j] = contents[i];
				j++;
			}
			indices = tmpIndices;
			contents = tmpContents;
		}
	}

	@Override
	public void clear() {
		size = 0;
	}

	@Override
	public int[] indicesOf(float content) {
		
		// TODO: This interrupt makes the developer aware of under-developed
		// components and get back to them later
		throw new NotImplementedException();
	}
}
