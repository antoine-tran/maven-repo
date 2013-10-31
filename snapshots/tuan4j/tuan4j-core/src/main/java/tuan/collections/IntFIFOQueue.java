package tuan.collections;

import java.util.NoSuchElementException;

/** 
 * A very small footprint FIFO Queue backed by a primitive array 
 * @since 24/09/2013
 * @author tuan 
 */
public class IntFIFOQueue {

	// cursor to the head of the queue
	private int head;
	
	// cursor to the first available space of the queue (right 
	// after the last element)
	private int tail;
	
	// data container
	private int[] data;
	
	private static final int DEFAULT_CAPACITY = 10;
	private static final float DEFAULT_LOAD_FACTOR = 1.5f;
	
	public IntFIFOQueue(int size) {
		data = new int[size];
		head = -1;
		tail = 0;
	}
	
	public IntFIFOQueue() {
		data = new int[DEFAULT_CAPACITY];
		head = -1;
		tail = 0;
	}
		
	/**
	 * Inserts the specified element into this queue if it is possible to do
	 * so immediately without violating capacity restrictions, returning true
	 * upon success and throwing an IllegalStateException if no space is
	 * currently available.
	 * @param e the element to add
	 * @throws IllegalStateException if the element cannot be added at this
	 * time due to capacity restrictions
	 */
	public boolean add(int e) throws IllegalStateException {		
		if (tail == data.length) {
			try {
				reallocateOrExpand();
			} catch (IndexOutOfBoundsException ex) {
				throw new IllegalStateException(ex);
			} catch (NullPointerException ex) {
				throw new IllegalStateException(ex);
			}
		}
		if (tail == data.length) {
			throw new IllegalStateException("Cannot add more element: The queue is too big");
		}
		data[tail] = e;
		tail++;
		return true;
	}

	/**
	 * Inserts the specified element into this queue if it is possible to do
	 * so immediately without violating capacity restrictions. When using a 
	 * capacity-restricted queue, this method is generally preferable to 
	 * {@link #add(int)}, which can fail to insert an element only by throwing
	 * an exception.
	 * @param e the element to add
	 * @return true (as specified by {@link java.util.Collection Collection.add(E)})
	 */
	public boolean offer(int e) {
		if (tail == data.length) {
			try {
				reallocateOrExpand();
			} catch (IndexOutOfBoundsException ex) {
				return false;
			} catch (NullPointerException ex) {
				return false;
			}
		}
		if (tail == data.length) {
			return false;
		}
		data[tail] = e;
		tail++;
		return true;
	}
	
	/**
	 * Retrieves and removes the head of this queue. This method differs from
	 * {@link #poll()} only in that it throws an exception if this queue is empty.
	 * @return the head of this queue
	 * @throws NoSuchElementException if this queue is empty
	 */
	public int remove() throws NoSuchElementException {
		if (isEmpty()) {
			throw new NoSuchElementException("Queue is empty");
		}
		int e = data[head];
		head++;
		return e;
	}
	
	/**
	 * Retrieves and removes the head of this queue, returns 
	 * <tt>Integer.MAX_VALUE</tt> if this queue is empty.
	 * @return head of this queue, or <tt>Integer.MAX_VALUE</tt> if this queue
	 * is empty
	 */
	public int poll() {
		if (isEmpty()) {
			return Integer.MAX_VALUE;
		}
		int e = data[head];
		head++;
		return e;		
	}
	
    /**
     * Retrieves, but does not remove, the head of this queue.  This method
     * differs from {@link #peek peek} only in that it throws an exception
     * if this queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */
	public int element() {
		if (isEmpty()) {
			throw new NoSuchElementException("Queue is empty");
		}
		return data[head];
	}
	
	/**
     * Retrieves, but does not remove, the head of this queue,
     * or returns <tt>Integer.MAX_VALUE</tt> if this queue is empty.
     *
     * @return the head of this queue, or <tt>Integer.MAX_VALUE</tt> if this
     * queue is empty
     */
    public int peek() {
		if (isEmpty()) {
			return Integer.MAX_VALUE;
		}
		return data[head];  	
    }
    
    /**
     * Check if the queue is empty
     */
    public boolean isEmpty() {
    	return (head == -1 || head == tail);
    }
    
    /**
     * expand the current queue. If the real data is small, just reallocate the 
     * elements to the same array, otherwise create a new array of the size
     * specified by a load factor and reallocate the elements
     */
    private synchronized void reallocateOrExpand() {
    	// do nothing if the queue is empty
    	if (isEmpty()) return;
    	
    	int size = tail - head;
    	if (size * DEFAULT_LOAD_FACTOR < data.length) {
    		for (int i = head; i < tail; i++) {
    			data[i-head] = data[i];
    		}
    	} else {
    		int newSize = (int) (data.length * DEFAULT_LOAD_FACTOR);
    		int[] newData = new int[newSize];
    		System.arraycopy(data, head, newData, 0, size);
    		data = newData;
    	}
		head = 0;
		tail = size;
    }
}
