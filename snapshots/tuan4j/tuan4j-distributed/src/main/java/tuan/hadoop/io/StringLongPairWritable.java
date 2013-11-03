package tuan.hadoop.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

/**
 * This class refactors the class PairOfStringLong of Jimmy
 * Lin's Cloud9 Map-Reduce librarry (http://lintool.github.com/Cloud9),
 * to make it more flexible to other frameworks.
 * 
 * @author tuan
 *
 */
public class StringLongPairWritable implements WritableComparable<StringLongPairWritable> {

	private String leftElement;
	private long rightElement;

	/**
	 * Creates a pair.
	 */
	public StringLongPairWritable() {}

	/**
	 * Creates a pair.
	 *
	 * @param left the left element
	 * @param right the right element
	 */
	public StringLongPairWritable(String left, long right) {
		set(left, right);
	}

	/**
	 * Deserializes the pair.
	 *
	 * @param in source for raw byte representation
	 */
	public void readFields(DataInput in) throws IOException {
		leftElement = Text.readString(in);
		rightElement = in.readLong();
	}

	/**
	 * Serializes this pair.
	 *
	 * @param out where to write the raw byte representation
	 */
	public void write(DataOutput out) throws IOException {
		Text.writeString(out, leftElement);
		out.writeLong(rightElement);
	}

	/**
	 * Returns the left element.
	 *
	 * @return the left element
	 */
	public String getLeftElement() {
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
	public String getKey() {
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
	public void set(String left, long right) {
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
		StringLongPairWritable pair = (StringLongPairWritable) obj;
		return leftElement.equals(pair.getLeftElement()) && rightElement == pair.getRightElement();
	}

	/**
	 * Defines a natural sort order for pairs. Pairs are sorted first by the
	 * left element, and then by the right element.
	 *
	 * @return a value less than zero, a value greater than zero, or zero if
	 *         this pair should be sorted before, sorted after, or is equal to
	 *         <code>obj</code>.
	 */
	public int compareTo(StringLongPairWritable pair) {
		String pl = pair.getLeftElement();
		long pr = pair.getRightElement();

		if (leftElement.equals(pl)) {
			if (rightElement == pr)
				return 0;

			return rightElement < pr ? -1 : 1;
		}

		return leftElement.compareTo(pl);
	}

	/**
	 * Returns a hash code value for the pair.
	 *
	 * @return hash code for the pair
	 */
	public int hashCode() {
		return leftElement.hashCode() + (int) (rightElement % Integer.MAX_VALUE);
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
	public StringLongPairWritable clone() {
		return new StringLongPairWritable(this.leftElement, this.rightElement);
	}

	/** Comparator optimized for <code>StringLongPairWritable</code>. */
	public static class Comparator extends WritableComparator {

		/**
		 * Creates a new Comparator optimized for <code>PairOfStrings</code>.
		 */
		public Comparator() {
			super(StringLongPairWritable.class);
		}

    /**
     * Optimization hook.
     */
    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
      try {
        int firstVIntL1 = WritableUtils.decodeVIntSize(b1[s1]);
        int firstVIntL2 = WritableUtils.decodeVIntSize(b2[s2]);
        int firstStrL1 = readVInt(b1, s1);
        int firstStrL2 = readVInt(b2, s2);
        int cmp = compareBytes(b1, s1 + firstVIntL1, firstStrL1, b2, s2 + firstVIntL2, firstStrL2);
        if (cmp != 0) {
          return cmp;
        }

        long thisRightValue = readLong(b1, s1 + firstVIntL1 + firstStrL1);
        long thatRightValue = readLong(b2, s2 + firstVIntL2 + firstStrL2);

        return (thisRightValue < thatRightValue ? -1 : (thisRightValue == thatRightValue ? 0 : 1));
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    }
	}

	static { // register this comparator
		WritableComparator.define(StringLongPairWritable.class, new Comparator());
	}
}
