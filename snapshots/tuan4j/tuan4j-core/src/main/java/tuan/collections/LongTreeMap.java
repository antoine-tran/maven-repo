package tuan.collections;

public class LongTreeMap<V> {

	private static final boolean RED   = true;
	private static final boolean BLACK = false;

	private Node root;     // root of the BST

	// BST helper node data type
	private class Node {
		private long key;           // key
		private V val;         // associated data
		private Node left, right;  // links to left and right subtrees
		private boolean color;     // color of parent link
		private int N;             // subtree count

		public Node(long key, V val, boolean color, int N) {
			this.key = key;
			this.val = val;
			this.color = color;
			this.N = N;
		}
	}

	/*************************************************************************
	 *  Node helper methods
	 *************************************************************************/
	// is node x red; false if x is null ?
	private boolean isRed(Node x) {
		if (x == null) return false;
		return (x.color == RED);
	}

	// number of node in subtree rooted at x; 0 if x is null
	private int size(Node x) {
		if (x == null) return 0;
		return x.N;
	} 


	/*************************************************************************
	 *  Size methods
	 *************************************************************************/

	// return number of key-value pairs in this symbol table
	public int size() { return size(root); }

	// is this symbol table empty?
	public boolean isEmpty() {
		return root == null;
	}

	/*************************************************************************
	 *  Standard BST search
	 *************************************************************************/

	// value associated with the given key; null if no such key
	public V get(long key) { return get(root, key); }

	// value associated with the given key in subtree rooted at x; null if no such key
	private V get(Node x, long key) {
		while (x != null) {
			if (key < x.key) x = x.left;
			else if (key > x.key) x = x.right;
			else return x.val;
		}
		return null;
	}

	// is there a key-value pair with the given key?
	public boolean contains(long key) {
		return (get(key) != null);
	}

	// is there a key-value pair with the given key in the subtree rooted at x?
	private boolean contains(Node x, long key) {
		return (get(x, key) != null);
	}
	
	public boolean contains()

	/*************************************************************************
	 *  Red-black insertion
	 *************************************************************************/

	// insert the key-value pair; overwrite the old value with the new value
	// if the key is already present
	public void put(long key, V val) {
		root = put(root, key, val);
		root.color = BLACK;
		assert check();
	}

	// insert the key-value pair in the subtree rooted at h
	private Node put(Node h, long key, V val) { 
		if (h == null) return new Node(key, val, RED, 1);

		if (key < h.key) h.left = put(h.left,  key, val); 
		else if (key > h.key) h.right = put(h.right, key, val); 
		else h.val = val;

		// fix-up any right-leaning links
		if (isRed(h.right) && !isRed(h.left)) h = rotateLeft(h);
		if (isRed(h.left)  &&  isRed(h.left.left)) h = rotateRight(h);
		if (isRed(h.left)  &&  isRed(h.right)) flipColors(h);
		h.N = size(h.left) + size(h.right) + 1;

		return h;
	}

	/*************************************************************************
	 *  Red-black deletion
	 *************************************************************************/

	// delete the key-value pair with the minimum key
	public void deleteMin() {
		if (isEmpty()) throw new RuntimeException("BST underflow");

		// if both children of root are black, set root to red
		if (!isRed(root.left) && !isRed(root.right))
			root.color = RED;

		root = deleteMin(root);
		if (!isEmpty()) root.color = BLACK;
		assert check();
	}

	// delete the key-value pair with the minimum key rooted at h
	private Node deleteMin(Node h) { 
		if (h.left == null)
			return null;

		if (!isRed(h.left) && !isRed(h.left.left))
			h = moveRedLeft(h);

		h.left = deleteMin(h.left);
		return balance(h);
	}


	// delete the key-value pair with the maximum key
	public void deleteMax() {
		if (isEmpty()) throw new RuntimeException("BST underflow");

		// if both children of root are black, set root to red
		if (!isRed(root.left) && !isRed(root.right))
			root.color = RED;

		root = deleteMax(root);
		if (!isEmpty()) root.color = BLACK;
		assert check();
	}

	// delete the key-value pair with the maximum key rooted at h
	private Node deleteMax(Node h) { 
		if (isRed(h.left))
			h = rotateRight(h);

		if (h.right == null)
			return null;

		if (!isRed(h.right) && !isRed(h.right.left))
			h = moveRedRight(h);

		h.right = deleteMax(h.right);

		return balance(h);
	}

	// delete the key-value pair with the given key
	public void delete(long key) { 
		if (!contains(key)) {
			System.err.println("symbol table does not contain " + key);
			return;
		}

		// if both children of root are black, set root to red
		if (!isRed(root.left) && !isRed(root.right))
			root.color = RED;

		root = delete(root, key);
		if (!isEmpty()) root.color = BLACK;
		assert check();
	}

	// delete the key-value pair with the given key rooted at h
	private Node delete(Node h, long key) { 
		assert contains(h, key);

		if (key < h.key)  {
			if (!isRed(h.left) && !isRed(h.left.left))
				h = moveRedLeft(h);
			h.left = delete(h.left, key);
		}
		else {
			if (isRed(h.left))
				h = rotateRight(h);
			if (key == h.key && (h.right == null))
				return null;
			if (!isRed(h.right) && !isRed(h.right.left))
				h = moveRedRight(h);
			if (key == h.key) {
				h.val = get(h.right, min(h.right).key);
				h.key = min(h.right).key;
				h.right = deleteMin(h.right);
			}
			else h.right = delete(h.right, key);
		}
		return balance(h);
	}

	/*************************************************************************
	 *  red-black tree helper functions
	 *************************************************************************/

	// make a left-leaning link lean to the right
	private Node rotateRight(Node h) {
		assert (h != null) && isRed(h.left);
		Node x = h.left;
		h.left = x.right;
		x.right = h;
		x.color = x.right.color;
		x.right.color = RED;
		x.N = h.N;
		h.N = size(h.left) + size(h.right) + 1;
		return x;
	}

	// make a right-leaning link lean to the left
	private Node rotateLeft(Node h) {
		assert (h != null) && isRed(h.right);
		Node x = h.right;
		h.right = x.left;
		x.left = h;
		x.color = x.left.color;
		x.left.color = RED;
		x.N = h.N;
		h.N = size(h.left) + size(h.right) + 1;
		return x;
	}

	// flip the colors of a node and its two children
	private void flipColors(Node h) {
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
	private Node moveRedLeft(Node h) {
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
	private Node moveRedRight(Node h) {
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
	private Node balance(Node h) {
		assert (h != null);

		if (isRed(h.right)) h = rotateLeft(h);
		if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
		if (isRed(h.left) && isRed(h.right)) flipColors(h);

		h.N = size(h.left) + size(h.right) + 1;
		return h;
	}


	/*************************************************************************
	 *  Utility functions
	 *************************************************************************/

	// height of tree; 0 if empty
	public int height() { return height(root); }
	private int height(Node x) {
		if (x == null) return 0;
		return 1 + Math.max(height(x.left), height(x.right));
	}

	/*************************************************************************
	 *  Ordered symbol table methods.
	 *************************************************************************/

	// the smallest key; Long.MIN_VALUE if no such key
	public long min() {
		if (isEmpty()) return Long.MIN_VALUE;
		return min(root).key;
	} 

	// the smallest key in subtree rooted at x; null if no such key
	private Node min(Node x) { 
		assert x != null;
		if (x.left == null) return x; 
		else return min(x.left); 
	} 

	// the largest key; Long.MAX_VALUE if no such key
	public long max() {
		if (isEmpty()) return Long.MAX_VALUE;
		return max(root).key;
	} 

	// the largest key in the subtree rooted at x; null if no such key
	private Node max(Node x) { 
		assert x != null;
		if (x.right == null) return x; 
		else return max(x.right); 
	} 

	// the largest key less than or equal to the given key
	// TODO: the return value Long.MAX_VALUE indicates that
	// the given key is the smallest in the tree
	public long floor(long key) {
		Node x = floor(root, key);
		if (x == null) return Long.MIN_VALUE;
		else return x.key;
	}    

	// the largest node in the subtree rooted at x having key less than or equal
	// to the given key
	private Node floor(Node x, long key) {
		if (x == null) return null;
		if (key == x.key) return x;
		if (key < x.key)  return floor(x.left, key);
		Node t = floor(x.right, key);
		if (t != null) return t; 
		else return x;
	}

	// the smallest key greater than or equal to the given key
	// TODO: The return value Long.MAX_VALUE indicates that the given key
	// is the greatest key in the tree
	public long ceiling(long key) {  
		Node x = ceiling(root, key);
		if (x == null) return Long.MAX_VALUE;
		else return x.key;  
	}

	// the smallest key in the subtree rooted at x greater than or equal to the given key
	private Node ceiling(Node x, long key) {  
		if (x == null) return null;
		if (key == x.key) return x;
		if (key > x.key)  return ceiling(x.right, key);
		Node t = ceiling(x.left, key);
		if (t != null) return t; 
		else return x;
	}


	// the key of rank k
	// TODO: The value of Long.MAX_VALUE indicates that
	// the current rank is negative, Long.MIN_VALUE
	// indicates that the current rank is to high
	public long select(int k) {
		if (k < 0)  return Long.MAX_VALUE;
		if (k >= size()) return Long.MIN_VALUE;
		Node x = select(root, k);
		return x.key;
	}

	// the key of rank k in the subtree rooted at x
	private Node select(Node x, int k) {
		assert x != null;
		assert k >= 0 && k < size(x);
		int t = size(x.left); 
		if (t > k) return select(x.left,  k); 
		else if (t < k) return select(x.right, k-t-1); 
		else return x; 
	} 

	// number of keys less than key
	public int rank(long key) {
		return rank(key, root);
	} 

	// number of keys less than key in the subtree rooted at x
	private int rank(long key, Node x) {
		if (x == null) return 0; 
		if (key < x.key) return rank(key, x.left); 
		else if (key > x.key) return 1 + size(x.left) + rank(key, x.right); 
		else return size(x.left); 
	} 

	/***********************************************************************
	 *  Range count and range search.
	 ***********************************************************************/

	// all of the keys, as an Iterable
	public long[] keys() {
		return keys(min(), max());
	}

	// the keys between lo and hi, as an Iterable
	public long[] keys(long lo, long hi) {
		LongArrayList queue = new LongArrayList(size());
		// if (isEmpty() || lo.compareTo(hi) > 0) return queue;
		keys(root, queue, lo, hi);
		return queue.toArray();
	} 

	// add the keys between lo and hi in the subtree rooted at x
	// to the queue
	private void keys(Node x, LongArrayList queue, long lo, long hi) { 
		if (x == null) return; 
		if (lo < x.key) keys(x.left, queue, lo, hi); 
		if (lo <= x.key && hi >= x.key) queue.add(x.key); 
		if (hi > x.key) keys(x.right, queue, lo, hi); 
	} 

	// number keys between lo and hi
	public int size(long lo, long hi) {
		if (lo > hi) return 0;
		if (contains(hi)) return rank(hi) - rank(lo) + 1;
		else return rank(hi) - rank(lo);
	}


	/*************************************************************************
	 *  Check integrity of red-black BST data structure
	 *************************************************************************/
	private boolean check() {
		return isBST() && isSizeConsistent() && isRankConsistent() && is23() && isBalanced();
	}

	// does this binary tree satisfy symmetric order?
	// Note: this test also ensures that data structure is a binary tree since order is strict
	private boolean isBST() {
		return isBST(root, Long.MIN_VALUE, Long.MAX_VALUE);
	}

	// is the tree rooted at x a BST with all keys strictly between min and max
	// (if min or max is null, treat as empty constraint)
	// Credit: Bob Dondero's elegant solution
	private boolean isBST(Node x, long min, long max) {
		if (x == null) return true;
		if (min != Long.MIN_VALUE && x.key <= min) return false;
		if (max != Long.MAX_VALUE && x.key >= max) return false;
		return isBST(x.left, min, x.key) && isBST(x.right, x.key, max);
	} 

	// are the size fields correct?
	private boolean isSizeConsistent() { return isSizeConsistent(root); }
	private boolean isSizeConsistent(Node x) {
		if (x == null) return true;
		if (x.N != size(x.left) + size(x.right) + 1) return false;
		return isSizeConsistent(x.left) && isSizeConsistent(x.right);
	} 

	// check that ranks are consistent
	private boolean isRankConsistent() {
		for (int i = 0; i < size(); i++)
			if (i != rank(select(i))) return false;
		for (long key : keys())
			if (key != select(rank(key))) return false;
		return true;
	}

	// Does the tree have no red right links, and at most one (left)
	// red links in a row on any path?
	private boolean is23() { return is23(root); }
	private boolean is23(Node x) {
		if (x == null) return true;
		if (isRed(x.right)) return false;
		if (x != root && isRed(x) && isRed(x.left))
			return false;
		return is23(x.left) && is23(x.right);
	} 

	// do all paths from root to leaf have same number of black edges?
	private boolean isBalanced() { 
		int black = 0;     // number of black links on path from root to min
		Node x = root;
		while (x != null) {
			if (!isRed(x)) black++;
			x = x.left;
		}
		return isBalanced(root, black);
	}

	// does every path from the root to a leaf have the given number of black links?
	private boolean isBalanced(Node x, int black) {
		if (x == null) return black == 0;
		if (!isRed(x)) black--;
		return isBalanced(x.left, black) && isBalanced(x.right, black);
	} 
}
