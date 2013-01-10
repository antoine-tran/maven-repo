package tuan.collections;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

/** A set backed by an array */
@SuppressWarnings("serial")
public class ArraySet<E> extends AbstractSet<E> implements Serializable {

	private E[] data;

	private Class<E> c;

	/** Tells whether the int is there*/
	protected BitSet isThere;

	/** Number of integers in data*/
	protected int numElements=0;

	/** Index pointing to an empty position. Handled exclusively by indexOf()*/
	protected int addIndex=0;

	/** Last index that contains a value*/
	protected int lastIndex=-1;
	
	/** A helper iterator */
	protected PrivateArrayIterator iterator;

	/** Adds the element*/
	public boolean add(E v) {
		// Use contains() also to set the addIndex
		if(contains(v)) return(false);  
		if(numElements==data.length) data=Arrays.copyOf(data,data.length+100);
		data[addIndex]=v;
		isThere.set(addIndex,true);
		numElements++;
		if(addIndex>lastIndex) lastIndex=addIndex;
		return(true);
	} 

	@SuppressWarnings("unchecked")
	public void clear(int capacity) {
		numElements=0;
		data = (E[]) Array.newInstance(c, capacity);
		isThere=new BitSet(capacity);
		lastIndex=-1;
	}

	/** Finds the element, sets addIndex to a free position*/
	protected int indexOf(E v) {
		addIndex=lastIndex+1;
		for(int i=0;i<=lastIndex;i++) {
			if(!isThere.get(i)) addIndex=i;
			if(data[i].equals(v) && isThere.get(i)) return(i);
		}
		return(-1);
	}

	/** Deletes empty space if necessary*/
	protected void shrink() {
		if(numElements<data.length/2 && data.length>300) {    
			ArraySet<E> result = new ArraySet<E>(c, numElements+100);
			result.addAll(this);
			this.data=result.data;
			this.isThere=result.isThere;
			this.lastIndex=result.lastIndex;
			this.numElements=result.numElements;
		}
	}

	/** Force to delete empty space*/
	public void trim() {
		if(numElements<data.length) {
			ArraySet<E> result=new ArraySet<E>(c, numElements);
			result.addAll(this);
			this.data=result.data;
			this.isThere=result.isThere;
			this.lastIndex=result.lastIndex;
			this.numElements=result.numElements;
		}
	}

	/** Force to shrink the data to a specified length.
	 * Return true if the data is modified*/
	public boolean trimToSize(int minSize) {
		if(numElements<=minSize) {
			ArraySet<E> result=new ArraySet<E>(c, minSize);
			result.addAll(this);
			this.data=result.data;
			this.isThere=result.isThere;
			this.lastIndex=result.lastIndex;
			this.numElements=result.numElements;
			return true;
		} else return false;
	}

	/** Removes an item*/
	protected E removeIndex(int index) {
		isThere.set(index, false);
		numElements--; 
		return(data[index]);
	}

	// ----------- Wrapper methods -------------

	/** Creates a new ArraySet from initial values.*/
	public static <E> ArraySet<E> asSet(Class<E> c, E... initial) {
		ArraySet<E> result=new ArraySet<E>(c);
		for(E i : initial) result.add(i);
		return(result);
	}

	/** Creates a new ArraySet by copying the given set*/
	public ArraySet(ArraySet<E> copy) {
		this.c = copy.c;
		setTo(copy);
	}

	/** Creates a new ArraySet. Since Java 6 does not support type
	 * inference, we have to parameterize the class in the constructor.
	 * This is not needed in Java 7 */
	public ArraySet(Class<E> c) {
		this.c = c;
		clear();
	}

	/** Creates a new ArraySet with an initial capacity. Since Java 6 does
	 * not support type inference, we have to parameterize the class in the
	 * constructor. This is not needed in Java 7 */
	public ArraySet(Class<E> c, int capacity) {
		this.c = c;
		clear(capacity);
	}

	/** Overwrites the current ArraySet with the given one*/
	public void setTo(ArraySet<E> copy) {
		if(copy==this) return;
		clear(copy.size());
		addAll(copy);
	}

	/** Removes one element, returns TRUE if the set was modified*/
	public boolean remove(Object i) {
		if (i == null) return false;
		try {
			E e = (E)i;
			int pos=indexOf(e);
			if(pos==-1) return false;
			removeIndex(pos);
			shrink();
			return true;   
		} catch (ClassCastException ex) {
			return false;
		} 
	} 

	/** Removes all elements in c*/
	public boolean removeAll(ArraySet<E> c) {
		boolean result=false;
		for(int i=0;i<=c.lastIndex;i++) {
			if(c.isThere.get(i)) result|=remove(c.data[i]);
		}
		return result;
	}

	/** Returns an array of the elements of this set
	 * NOTE: This will create a new array to protect underlying data*/
	public E[] toArray() {
		E[] result = (E[]) Array.newInstance(c, numElements);
		int j=0;
		for(int i=0;i<=lastIndex;i++) {
			if(isThere.get(i)) result[j++]=data[i]; 
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder res=new StringBuilder("[");
		for(int i=0;i<=lastIndex;i++) {
			if(isThere.get(i)) res.append(data[i]).append(", ");
		}
		if(numElements>0) res.setLength(res.length()-2);    
		return res.append("]").toString();
	}    

	public int size() {
		return numElements;
	}

	/** Adds all elements*/
	public boolean addAll(E[] c) {
		boolean returnValue=false;
		for(E i : c) returnValue|=add(i);
		return(returnValue);
	}

	/** Adds all elements*/
	public boolean addAll(ArraySet<E> s) {
		boolean returnValue=false;
		for(int index=0;index<=s.lastIndex;index++) {
			if(s.isThere.get(index)) returnValue|=add(s.data[index]);
		}
		return(returnValue);
	}

	/** Removes all elements in c*/
	public boolean removeAll(E[] c) {
		boolean result=false;
		for(E o : c) result|=remove(o);
		return result;
	}
	
	/** Adds all elements, returns THIS*/  
	public ArraySet<E> enhancedBy(ArraySet<E> addMe) {
		addAll(addMe);
		return(this);
	}


	/** Removes the given elements, returns THIS*/
	public ArraySet<E> shrunkBy(ArraySet<E> addMe) {
		removeAll(addMe);
		return(this);
	}

	/** TRUE if the set contains i*/
	@Override
	public boolean contains(Object i) {
		if (i == null) return false;
		try {
			E e = (E)i;
			return(indexOf(e)!=-1);
		} catch (ClassCastException ex) {
			return false;
		} 
	}

	/** TRUE if the set is empty*/
	public boolean isEmpty() {
		return numElements==0;
	}

	/** Removes all elements*/
	public void clear() {
		clear(10);
	} 

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	private final class PrivateArrayIterator implements Iterator<E> {
		
		/** Cursor pointer */
		private int cursor;
		
		private PrivateArrayIterator() {			
			cursor = 0;
		}
		
		@Override
		public boolean hasNext() {			
			return (cursor <= lastIndex && lastIndex != -1);
		}

		@Override
		public E next() {
			if (cursor > lastIndex) 
				throw new IndexOutOfBoundsException(" read off the set: " + cursor);
			if (lastIndex == -1) 
				throw new IndexOutOfBoundsException(" Set is empty: " + lastIndex);
			if (isThere.get(cursor)) {
				E e = data[cursor];
				while (!isThere.get(++cursor)) {}
				return e;
			}
			else throw new RuntimeException("item removed: " + cursor);
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub

		}

	}
}
