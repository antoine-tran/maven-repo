package tuan.hadoop.io;

/**
 * A lightweight pair of integers
 * 
 * @author tuan
 *
 */
public class IntPair {
	
	private int left, right;

	public IntPair() {}
	
	public IntPair(int left, int right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * @return the left
	 */
	public int getLeft() {
		return left;
	}

	/**
	 * @param left the left to set
	 */
	public void setLeft(int left) {
		this.left = left;
	}

	/**
	 * @return the right
	 */
	public int getRight() {
		return right;
	}

	/**
	 * @param right the right to set
	 */
	public void setRight(int right) {
		this.right = right;
	}
}
