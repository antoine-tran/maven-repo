/**
 * 
 */
package tuan.hadoop.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

/**
 * This class refactors the class PairOfStrings of Jimmy
 * Lin's Cloud9 Map-Reduce librarry (http://lintool.github.com/Cloud9),
 * to make it more flexible to other frameworks.
 * 
 * @author tuan
 *
 */
public class StringPairWritable implements WritableComparable<StringPairWritable> {

	private String leftElement, rightElement;

	/**
	 * Creates a pair.
	 */
	public StringPairWritable() {}

	/**
	 * Creates a pair.
	 *
	 * @param left the left element
	 * @param right the right element
	 */
	public StringPairWritable(String left, String right) {
		set(left, right);
	}

	/**
	 * Deserializes the pair.
	 *
	 * @param in source for raw byte representation
	 */
	public void readFields(DataInput in) throws IOException {
		leftElement = Text.readString(in);
		rightElement = Text.readString(in);
	}

	/**
	 * Serializes this pair.
	 *
	 * @param out where to write the raw byte representation
	 */
	public void write(DataOutput out) throws IOException {
		Text.writeString(out, leftElement);
		Text.writeString(out, rightElement);
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
	public String getRightElement() {
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
	public String getValue() {
		return rightElement;
	}

	/**
	 * Sets the right and left elements of this pair.
	 *
	 * @param left the left element
	 * @param right the right element
	 */
	public void set(String left, String right) {
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
		if (obj == this) return true;
		else if (obj == null || !(obj instanceof StringPairWritable)) return false;
		StringPairWritable pair = (StringPairWritable) obj;
		return leftElement.equals(pair.getLeftElement())
				&& rightElement.equals(pair.getRightElement());
	}

	/**
	 * Defines a natural sort order for pairs. Pairs are sorted first by the
	 * left element, and then by the right element.
	 *
	 * @return a value less than zero, a value greater than zero, or zero if
	 *         this pair should be sorted before, sorted after, or is equal to
	 *         <code>obj</code>.
	 */
	public int compareTo(StringPairWritable pair) {
		String pl = pair.getLeftElement();
		String pr = pair.getRightElement();

		if (leftElement.equals(pl)) {
			return rightElement.compareTo(pr);
		}

		return leftElement.compareTo(pl);
	}

	/**
	 * Returns a hash code value for the pair.
	 *
	 * @return hash code for the pair
	 */
	public int hashCode() {
		return leftElement.hashCode() + rightElement.hashCode();
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
	public StringPairWritable clone() {
		return new StringPairWritable(this.leftElement, this.rightElement);
	}

	/** Comparator optimized for <code>StringPairWritable</code>. */
	public static class Comparator extends WritableComparator {

		/**
		 * Creates a new Comparator optimized for <code>StringPairWritable</code>.
		 */
		public Comparator() {
			super(StringPairWritable.class);
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

        int secondVIntL1 = WritableUtils.decodeVIntSize(b1[s1 + firstVIntL1 + firstStrL1]);
        int secondVIntL2 = WritableUtils.decodeVIntSize(b2[s2 + firstVIntL2 + firstStrL2]);
        int secondStrL1 = readVInt(b1, s1 + firstVIntL1 + firstStrL1);
        int secondStrL2 = readVInt(b2, s2 + firstVIntL2 + firstStrL2);
        return compareBytes(b1, s1 + firstVIntL1 + firstStrL1 + secondVIntL1, secondStrL1, b2,
            s2 + firstVIntL2 + firstStrL2 + secondVIntL2, secondStrL2);
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

	static { // register this comparator
		WritableComparator.define(StringPairWritable.class, new Comparator());
	}
}
