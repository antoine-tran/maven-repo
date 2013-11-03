package tuan.hadoop.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * This class refactors the class PairOfIntLong of Jimmy
 * Lin's Cloud9 Map-Reduce librarry (http://lintool.github.com/Cloud9),
 * to make it more flexible to other frameworks.
 * 
 * @author tuan
 *
 */
public class IntLongPairWritable implements WritableComparable<IntLongPairWritable> {
	private int leftElement;
	private long rightElement;

	/**
	 * Creates a pair.
	 */
	public IntLongPairWritable() {}

	/**
	 * Creates a pair.
	 *
	 * @param left the left element
	 * @param right the right element
	 */
	public IntLongPairWritable(int left, long right) {
		set(left, right);
	}

	/**
	 * Deserializes this pair.
	 *
	 * @param in source for raw byte representation
	 */
	public void readFields(DataInput in) throws IOException {
		leftElement = in.readInt();
		rightElement = in.readLong();
	}

	/**
	 * Serializes this pair.
	 *
	 * @param out where to write the raw byte representation
	 */
	public void write(DataOutput out) throws IOException {
		out.writeInt(leftElement);
		out.writeLong(rightElement);
	}

	/**
	 * Returns the left element.
	 *
	 * @return the left element
	 */
	public int getLeftElement() {
		return leftElement;
	}

	/**
	 * Returns the right element.
	 *
	 * @return the right element
	 */
	public long getRightElement() {
		return rightElement;
	}

	/**
	 * Returns the key (left element).
	 *
	 * @return the key
	 */
	public int getKey() {
		return leftElement;
	}

	/**
	 * Returns the value (right element).
	 *
	 * @return the value
	 */
	public long getValue() {
		return rightElement;
	}

	/**
	 * Sets the right and left elements of this pair.
	 *
	 * @param left the left element
	 * @param right the right element
	 */
	public void set(int left, long right) {
		leftElement = left;
		rightElement = right;
	}

	/**
	 * Checks two pairs for equality.
	 *
	 * @param obj object for comparison
	 * @return <code>true</code> if <code>obj</code> is equal to this object, <code>false</code> otherwise
	 */
	public boolean equals(Object obj) {
		IntLongPairWritable pair = (IntLongPairWritable) obj;
		return leftElement == pair.getLeftElement() && rightElement == pair.getRightElement();
	}

	/**
	 * Defines a natural sort order for pairs. Pairs are sorted first by the
	 * left element, and then by the right element.
	 *
	 * @return a value less than zero, a value greater than zero, or zero if
	 *         this pair should be sorted before, sorted after, or is equal to
	 *         <code>obj</code>.
	 */
	public int compareTo(IntLongPairWritable pair) {
		int pl = pair.getLeftElement();
		long pr = pair.getRightElement();

		if (leftElement == pl) {
			if (rightElement < pr)
				return -1;
			if (rightElement > pr)
				return 1;
			return 0;
		}

		if (leftElement < pl)
			return -1;

		return 1;
	}

	/**
	 * Returns a hash code value for the pair.
	 *
	 * @return hash code for the pair
	 */
	public int hashCode() {
		return (int) (leftElement + rightElement);
	}

	/**
	 * Generates human-readable String representation of this pair.
	 *
	 * @return human-readable String representation of this pair
	 */
	public String toString() {
		return "(" + leftElement + ", " + rightElement + ")";
	}

	/**
	 * Clones this object.
	 *
	 * @return clone of this object
	 */
	public IntLongPairWritable clone() {
		return new IntLongPairWritable(this.leftElement, this.rightElement);
	}

	/** Comparator optimized for <code>IntLongPairWritable</code>. */
	public static class Comparator extends WritableComparator {

		/**
		 * Creates a new Comparator optimized for <code>IntLongPairWritable</code>.
		 */
		public Comparator() {
			super(IntLongPairWritable.class);
		}

		/**
		 * Optimization hook.
		 */
		public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
			int thisLeftValue = readInt(b1, s1);
			int thatLeftValue = readInt(b2, s2);

			if (thisLeftValue == thatLeftValue) {
				long thisRightValue = readLong(b1, s1 + 4);
				long thatRightValue = readLong(b2, s2 + 4);

				return (thisRightValue < thatRightValue ? -1
						: (thisRightValue == thatRightValue ? 0 : 1));
			}

			return (thisLeftValue < thatLeftValue ? -1 : (thisLeftValue == thatLeftValue ? 0 : 1));
		}
	}

	static { // register this comparator
		WritableComparator.define(IntLongPairWritable.class, new Comparator());
	}
}
