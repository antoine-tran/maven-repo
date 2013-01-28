package tuan.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;


/** A space-efficient red-black tree-based map as equivalent to TreeMap<K,V>.
 * It guarantees much less memory consumption due to un-boxing / boxing avoidance. The 
 * implementation is adapted from the Java source code at "Algorithms", 4th Edition by
 * Robert Sedgewick and Kevin Wayne and based on Java OpenJDK 6-b14
 * 
 * @author tuan
 *
 * @param <V>
 */
public class IntObjectTreeMap<V> {

	// Node types
	private static final boolean RED = true;
	private static final boolean BLACK = false;

	// root of the BST
	private transient IntObjectEntry<V> root;     

	// An pseudo-object that stores the most recently removed node
	private IntObjectEntry<V> removedNode;
	private V removedVal;

	// Basic data structure for the red-black tree
	final static class IntObjectEntry<V> {
		private int key;       
		private V val;         
		private IntObjectEntry<V> left, right;

		// color of parent link
		private boolean color;

		// subtree count
		private int childCnt;             

		public IntObjectEntry(int key, V val, boolean color, int subTreeCnt) {
			this.key = key;
			this.val = val;
			this.color = color;
			this.childCnt = subTreeCnt;
		}

		@Override
		public boolean equals(Object e) {
			if (e == this) return true;
			if (e == null || !(e instanceof IntObjectTreeMap.IntObjectEntry)) return false;

			@SuppressWarnings("unchecked")
			IntObjectEntry<V> obj = (IntObjectTreeMap.IntObjectEntry<V>)e;

			boolean equals = (key == obj.key);
			if (val == null && obj.val == null) return equals;
			else return equals && val.equals(obj.val); 
		}

		@Override
		public int hashCode() {
			return key*31 + ((val != null) ? val.hashCode() : 0);
		}
		
		public int getKey() {
			return key;
		}
		
		public V getValue() {
			return val;
		}
		
		public void setValue(V val) {
			this.val = val;
		}
	}

	// true if node x is red; false if x is null ?
	private boolean isRed(IntObjectEntry<V> x) {
		if (x == null) return false;
		return (x.color == RED);
	}

	// number of node in subtree rooted at x; 0 if x is null
	private int size(IntObjectEntry<V> x) {
		if (x == null) return 0;
		return x.childCnt;
	} 

	/** return number of key-value pairs in this symbol table */
	public int size() {
		return size(root); 
	}

	/** check if current tree is empty */
	public boolean isEmpty() {
		return root == null;
	}

	/** value associated with the given key; null if no such key */
	public V get(int key) { return get(root, key); }

	// value associated with the given key in subtree rooted at x; null if no such key
	private V get(IntObjectEntry<V> x, int key) {
		while (x != null) {
			if (key < x.key) x = x.left;
			else if (key > x.key) x = x.right;
			else return x.val;
		}
		return null;
	}

	/** check if there is a key-value pair with the given key */
	public boolean containsKey(int key) {
		return (get(key) != null);
	}

	// is there a key-value pair with the given key in the subtree rooted at x?
	public boolean containsKey(IntObjectEntry<V> x, int key) {
		return (get(x, key) != null);
	}


	/** insert the key-value pair; overwrite the old value with the new value
	 * if the key is already present */
	public void put(int key, V val) {
		root = put(root, key, val);
		root.color = BLACK;
		assert check();
	}

	// insert the key-value pair in the subtree rooted at h
	private IntObjectEntry<V> put(IntObjectEntry<V> h, int key, V val) { 
		if (h == null) return new IntObjectEntry<V>(key, val, RED, 1);

		if (key < h.key) h.left = put(h.left,  key, val); 
		else if (key > h.key) h.right = put(h.right, key, val); 
		else h.val = val;

		// fix-up any right-leaning links
		if (isRed(h.right) && !isRed(h.left)) 
			h = rotateLeft(h);
		if (isRed(h.left)  &&  isRed(h.left.left)) 
			h = rotateRight(h);
		if (isRed(h.left)  &&  isRed(h.right)) 
			flipColors(h);
		h.childCnt = size(h.left) + size(h.right) + 1;

		return h;
	}

	/** Removes and returns a key-value mapping associated with the least key in
	 * this map, or null if the map is empty. */
	public IntObjectEntry<V> pollFirstEntry() {
		if (isEmpty()) return null;

		// if both children of root are black, set root to red
		if (!isRed(root.left) && !isRed(root.right))
			root.color = RED;

		root = deleteMin(root);
		if (!isEmpty()) root.color = BLACK;
		assert check();
		return removedNode;
	}

	// delete the key-value pair with the minimum key rooted at h
	private IntObjectEntry<V> deleteMin(IntObjectEntry<V> h) { 
		if (h.left == null) {
			removedNode = h;
			return null;
		}

		if (!isRed(h.left) && !isRed(h.left.left))
			h = moveRedLeft(h);

		h.left = deleteMin(h.left);
		return balance(h);
	}


	/** Removes and returns a key-value mapping associated with the greatest key in
	 * this map, or null if the map is empty */
	public IntObjectEntry<V> pollLastEntry() {
		if (isEmpty()) return null;

		// if both children of root are black, set root to red
		if (!isRed(root.left) && !isRed(root.right))
			root.color = RED;

		root = deleteMax(root);
		if (!isEmpty()) root.color = BLACK;
		assert check();
		return removedNode;
	}

	// delete the key-value pair with the maximum key rooted at h
	private IntObjectEntry<V> deleteMax(IntObjectEntry<V> h) { 
		if (isRed(h.left))
			h = rotateRight(h);

		if (h.right == null) {
			removedNode = h;
			return null;
		}


		if (!isRed(h.right) && !isRed(h.right.left))
			h = moveRedRight(h);

		h.right = deleteMax(h.right);

		return balance(h);
	}

	/** 
	 * Removes the mapping for this key from this TreeMap if present.
	 * @return the previous value associated with key, or null if there
	 *  was no mapping for key. (A null return can also indicate that 
	 *  the map previously associated null with key.)  */
	public V remove(int key) { 
		if (!containsKey(key)) {			
			return null;
		}

		// if both children of root are black, set root to red
		if (!isRed(root.left) && !isRed(root.right))
			root.color = RED;

		root = delete(root, key);
		if (!isEmpty()) root.color = BLACK;
		assert check();
		return removedVal;
	}

	// delete the key-value pair with the given key rooted at h
	private IntObjectEntry<V> delete(IntObjectEntry<V> h, int key) { 
		//assert contains(h, key);

		if (key < h.key)  {
			if (!isRed(h.left) && !isRed(h.left.left))
				h = moveRedLeft(h);
			h.left = delete(h.left, key);
		}
		else {
			if (isRed(h.left))
				h = rotateRight(h);
			if (key == h.key && (h.right == null)) {
				removedVal = h.val;
				return null;
			}				
			if (!isRed(h.right) && !isRed(h.right.left))
				h = moveRedRight(h);
			if (key == h.key) {				
				int k = min(h.right).key;
				V val = h.val;
				h.val = get(h.right, k);
				h.key = k;
				h.right = deleteMin(h.right);
				removedVal = val;
			}
			else h.right = delete(h.right, key);
		}
		return balance(h);
	}

	// make a left-leaning link lean to the right
	private IntObjectEntry<V> rotateRight(IntObjectEntry<V> h) {
		assert (h != null) && isRed(h.left);
		IntObjectEntry<V> x = h.left;
		h.left = x.right;
		x.right = h;
		x.color = x.right.color;
		x.right.color = RED;
		x.childCnt = h.childCnt;
		h.childCnt = size(h.left) + size(h.right) + 1;
		return x;
	}

	// make a right-leaning link lean to the left
	private IntObjectEntry<V> rotateLeft(IntObjectEntry<V> h) {
		assert (h != null) && isRed(h.right);
		IntObjectEntry<V> x = h.right;
		h.right = x.left;
		x.left = h;
		x.color = x.left.color;
		x.left.color = RED;
		x.childCnt = h.childCnt;
		h.childCnt = size(h.left) + size(h.right) + 1;
		return x;
	}

	// flip the colors of a node and its two children
	private void flipColors(IntObjectEntry<V> h) {
		// h must have opposite color of its two children
		assert (h != null) && (h.left != null) && (h.right != null);
		assert (!isRed(h) &&  isRed(h.left) &&  isRed(h.right))
		|| (isRed(h)  && !isRed(h.left) && !isRed(h.right));
		h.color = !h.color;
		h.left.color = !h.left.color;
		h.right.color = !h.right.color;
	}

	// Assuming that h is red and both h.left and h.left.left
	// are black, make h.left or one of its children red.
	private IntObjectEntry<V> moveRedLeft(IntObjectEntry<V> h) {
		assert (h != null);
		assert isRed(h) && !isRed(h.left) && !isRed(h.left.left);

		flipColors(h);
		if (isRed(h.right.left)) { 
			h.right = rotateRight(h.right);
			h = rotateLeft(h);
			// flipColors(h);
		}
		return h;
	}

	// Assuming that h is red and both h.right and h.right.left
	// are black, make h.right or one of its children red.
	private IntObjectEntry<V> moveRedRight(IntObjectEntry<V> h) {
		assert (h != null);
		assert isRed(h) && !isRed(h.right) && !isRed(h.right.left);
		flipColors(h);
		if (isRed(h.left.left)) { 
			h = rotateRight(h);
			// flipColors(h);
		}
		return h;
	}

	// restore red-black tree invariant
	private IntObjectEntry<V> balance(IntObjectEntry<V> h) {
		assert (h != null);

		if (isRed(h.right)) h = rotateLeft(h);
		if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
		if (isRed(h.left) && isRed(h.right)) flipColors(h);

		h.childCnt = size(h.left) + size(h.right) + 1;
		return h;
	}

	/** return the height of tree; 0 if empty */
	public int height() { 
		return height(root); 
	}

	private int height(IntObjectEntry<V> x) {
		if (x == null) return 0;
		return 1 + Math.max(height(x.left), height(x.right));
	}

	/** Returns the first (lowest) key currently in this map, or 
	 * Integer.MIN_VALUE if the tree is empty */
	public int firstKey() {
		if (isEmpty()) return Integer.MIN_VALUE;
		return min(root).key;
	} 

	// the smallest key in subtree rooted at x; null if no such key
	private IntObjectEntry<V> min(IntObjectEntry<V> x) { 
		assert x != null;
		if (x.left == null) return x; 
		else return min(x.left); 
	} 

	/** Returns the last (highest) key currently in this map, or 
	 * Integer.MAX_VALUE if the tree is empty */
	public int lastKey() {
		if (isEmpty()) return Integer.MAX_VALUE;
		return max(root).key;
	} 

	// the largest key in the subtree rooted at x; null if no such key
	private IntObjectEntry<V> max(IntObjectEntry<V> x) { 
		assert x != null;
		if (x.right == null) return x; 
		else return max(x.right); 
	} 

	/**  Returns the greatest key less than or equal to the given key, 
	 * or Integer.MIN_VALUE if there is no such key. */	
	public int floorKey(int key) {
		IntObjectEntry<V> x = floor(root, key);
		if (x == null) return Integer.MIN_VALUE;
		else return x.key;
	}    

	// the largest node in the subtree rooted at x having key less than or equal
	// to the given key
	private IntObjectEntry<V> floor(IntObjectEntry<V> x, int key) {
		if (x == null) return null;
		if (key == x.key) return x;
		if (key < x.key)  return floor(x.left, key);
		IntObjectEntry<V> t = floor(x.right, key);
		if (t != null) return t; 
		else return x;
	}

	/** Returns the least key greater than or equal to the given key, or Integer.MAX_VALUE
	 * if there is no such key. */
	public int ceilingKey(int key) {  
		IntObjectEntry<V> x = ceiling(root, key);
		if (x == null) return Integer.MAX_VALUE;
		else return x.key;  
	}

	// the smallest key in the subtree rooted at x greater than or equal to the given key
	private IntObjectEntry<V> ceiling(IntObjectEntry<V> x, int key) {  
		if (x == null) return null;
		if (key == x.key) return x;
		if (key > x.key)  return ceiling(x.right, key);
		IntObjectEntry<V> t = ceiling(x.left, key);
		if (t != null) return t; 
		else return x;
	}

	/** the key of rank k
	 * The value of Integer.MAX_VALUE indicates that
	 * the current rank is negative, Integer.MIN_VALUE
	 * indicates that the current rank is to high
	 */
	public int select(int k) {
		if (k < 0)  return Integer.MAX_VALUE;
		if (k >= size()) return Integer.MIN_VALUE;
		IntObjectEntry<V> x = select(root, k);
		return x.key;
	}

	// the key of rank k in the subtree rooted at x
	private IntObjectEntry<V> select(IntObjectEntry<V> x, int k) {
		assert x != null;
		assert k >= 0 && k < size(x);
		int t = size(x.left); 
		if (t > k) return select(x.left,  k); 
		else if (t < k) return select(x.right, k-t-1); 
		else return x; 
	} 

	/** number of keys less than the given key */
	public int rank(int key) {
		return rank(key, root);
	} 

	// number of keys less than key in the subtree rooted at x
	private int rank(int key, IntObjectEntry<V> x) {
		if (x == null) return 0; 
		if (key < x.key) return rank(key, x.left); 
		else if (key > x.key) return 1 + size(x.left) + rank(key, x.right); 
		else return size(x.left); 
	} 

	/** return the key array of this map in ascending order */
	public int[] keys() {
		return keys(firstKey(), lastKey());
	}

	/** the keys between lo and hi, as a primitive array */
	public int[] keys(int lo, int hi) {
		IntArrayList queue = new IntArrayList(size());
		// if (isEmpty() || lo.compareTo(hi) > 0) return queue;
		keys(root, queue, lo, hi);
		return queue.toArray();
	}

	// add the keys between lo and hi in the subtree rooted at x
	// to the queue
	private void keys(IntObjectEntry<V> x, IntArrayList queue, int lo, int hi) { 
		if (x == null) return; 
		if (lo < x.key) keys(x.left, queue, lo, hi); 
		if (lo <= x.key && hi >= x.key) queue.add(x.key); 
		if (hi > x.key) keys(x.right, queue, lo, hi); 
	}

	/** return the key array of this map in descending order */
	public int[] descendingKeys() {
		return descendingKeys(firstKey(), lastKey());
	}

	/** the keys between lo and hi, as a primitive array in 
	 * descending order */
	public int[] descendingKeys(int lo, int hi) {
		IntArrayList queue = new IntArrayList(size());
		// if (isEmpty() || lo.compareTo(hi) > 0) return queue;
		descendingKeys(root, queue, lo, hi);
		return queue.toArray();
	}

	// add the keys between lo and hi in the subtree rooted at x
	// to the queue. Keys are added in descending order
	private void descendingKeys(IntObjectEntry<V> x, IntArrayList queue,
			int lo, int hi) { 
		if (x == null) return; 
		if (hi > x.key) keys(x.right, queue, lo, hi); 
		if (lo <= x.key && hi >= x.key) queue.add(x.key); 
		if (lo < x.key) keys(x.left, queue, lo, hi); 
	}
	
	// traverse the tree rooted at h in left-first order and feed data to the buffer
	private void traverseRightFirst(IntObjectEntry<V> h, ArrayList<V> buffer) {
		if (h == null) return;
		traverseRightFirst(h.right, buffer);
		buffer.add(h.val);
		traverseRightFirst(h.left, buffer);
	}

	/** Returns a Set view of the keys contained in this map. */
	public IntSet keySet() {
		return keySet(firstKey(), lastKey());
	}

	/** Returns a Set view of the keys contained in this map between lo and hi */
	public IntSet keySet(int lo, int hi) {
		IntSet set = new IntSet(size());
		keys(root, set, lo, hi);
		return set;
	}

	// add the keys between lo and hi in the subtree rooted at x
	// to the queue
	private void keys(IntObjectEntry<V> x, IntSet queue, int lo, int hi) { 
		if (x == null) return; 
		if (lo < x.key) keys(x.left, queue, lo, hi); 
		if (lo <= x.key && hi >= x.key) queue.add(x.key); 
		if (hi > x.key) keys(x.right, queue, lo, hi); 
	}

	/** 
	 * Returns an Collection view of the values contained in this map in ascending
	 * order of the corresponding keys, or null if the tree is empty. This method
	 * is different from standard {@link Java.util.TreeMap.values()} implementation in
	 * that it guarantees immutability, i.e. changes in the collection view are
	 * not reflected back into the map
	 */
	public Collection<V> values() {		
		if (root == null) return null;
		ArrayList<V> res = new ArrayList<V>(size());
		traverseLeftFirst(root, res);
		return res;
	}

	// traverse the tree rooted at h in left-first order and feed data to the buffer
	private void traverseLeftFirst(IntObjectEntry<V> h, ArrayList<V> buffer) {
		if (h == null) return;
		traverseLeftFirst(h.left, buffer);
		buffer.add(h.val);
		traverseLeftFirst(h.right, buffer);
	}

	/** Returns an Iterator view of the values contained in this map in descending
	 *  order of the corresponding keys.  This method is different from standard
	 *  {@link Java.util.TreeMap.values()} implementation in that it guarantees
	 *  immutability, i.e. changes in the collection view are
	 * not reflected back into the map
	 * */
	public Collection<V> descendingValues() {
		if (root == null) return null;
		ArrayList<V> res = new ArrayList<V>(size());
		traverseRightFirst(root, res);
		return res;
	}

	/** Returns a Set view of the mappings contained in this map. The set's iterator
	 *  returns the entries in descending key order. This method is different from standard
	 *  {@link Java.util.TreeMap.entrySet()} implementation in that it guarantees
	 *  immutability, i.e. changes in the collection view are
	 *  not reflected back into the map */
	public Set<IntObjectEntry<V>> descendingEntrySet() {
		if (root == null) return null;
		TreeSet<IntObjectEntry<V>> set = new TreeSet<IntObjectEntry<V>>();
		traverseRightFirst(root, set);
		return set;
	}

	// traverse the tree rooted at h in left-first order and feed data to the buffer
	private void traverseRightFirst(IntObjectEntry<V> h, TreeSet<IntObjectEntry<V>> buffer) {
		if (h == null) return;
		traverseRightFirst(h.right, buffer);
		buffer.add(h);
		traverseRightFirst(h.left, buffer);
	}
	
	/** Returns a Set view of the mappings contained in this map. The set's iterator
	 *  returns the entries in ascending key order. This method is different from standard
	 *  {@link Java.util.TreeMap.entrySet()} implementation in that it guarantees
	 *  immutability, i.e. changes in the collection view are
	 *  not reflected back into the map */
	public Set<IntObjectEntry<V>> entrySet() {
		if (root == null) return null;
		TreeSet<IntObjectEntry<V>> set = new TreeSet<IntObjectEntry<V>>();
		traverseLeftFirst(root, set);
		return set;
	}

	// traverse the tree rooted at h in left-first order and feed data to the buffer
	private void traverseLeftFirst(IntObjectEntry<V> h, TreeSet<IntObjectEntry<V>> buffer) {
		if (h == null) return;
		traverseLeftFirst(h.left, buffer);
		buffer.add(h);
		traverseLeftFirst(h.right, buffer);
	}

	/** number keys between lo and hi */
	public int size(int lo, int hi) {
		if (lo > hi) return 0;
		if (containsKey(hi)) return rank(hi) - rank(lo) + 1;
		else return rank(hi) - rank(lo);
	}

	private boolean check() {
		return isBST() && isSizeConsistent() && isRankConsistent() && is23() && isBalanced();
	}

	// does this binary tree satisfy symmetric order?
	// Note: this test also ensures that data structure is a binary tree since order is strict
	private boolean isBST() {
		return isBST(root, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	// is the tree rooted at x a BST with all keys strictly between min and max
	// (if min or max is null, treat as empty constraint)
	// Credit: Bob Dondero's elegant solution
	private boolean isBST(IntObjectEntry<V> x, int min, int max) {
		if (x == null) return true;
		if (min != Integer.MIN_VALUE && x.key <= min) return false;
		if (max != Integer.MAX_VALUE && x.key >= max) return false;
		return isBST(x.left, min, x.key) && isBST(x.right, x.key, max);
	} 

	// are the size fields correct?
	private boolean isSizeConsistent() { return isSizeConsistent(root); }
	private boolean isSizeConsistent(IntObjectEntry<V> x) {
		if (x == null) return true;
		if (x.childCnt != size(x.left) + size(x.right) + 1) return false;
		return isSizeConsistent(x.left) && isSizeConsistent(x.right);
	} 

	// check that ranks are consistent
	private boolean isRankConsistent() {
		for (int i = 0; i < size(); i++)
			if (i != rank(select(i))) return false;
		for (int key : keys())
			if (key != select(rank(key))) return false;
		return true;
	}

	// Does the tree have no red right links, and at most one (left)
	// red links in a row on any path?
	private boolean is23() { return is23(root); }
	private boolean is23(IntObjectEntry<V> x) {
		if (x == null) return true;
		if (isRed(x.right)) return false;
		if (x != root && isRed(x) && isRed(x.left))
			return false;
		return is23(x.left) && is23(x.right);
	} 

	// do all paths from root to leaf have same number of black edges?
	private boolean isBalanced() { 
		int black = 0;     // number of black links on path from root to min
		IntObjectEntry<V> x = root;
		while (x != null) {
			if (!isRed(x)) black++;
			x = x.left;
		}
		return isBalanced(root, black);
	}

	// does every path from the root to a leaf have the given number of black links?
	private boolean isBalanced(IntObjectEntry<V> x, int black) {
		if (x == null) return black == 0;
		if (!isRed(x)) black--;
		return isBalanced(x.left, black) && isBalanced(x.right, black);
	} 
}
