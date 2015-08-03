package tuan.hadoop.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

public class StringTripleWritable implements WritableComparable<StringTripleWritable> {

	private String leftElement, rightElement;
	private String third;

	public StringTripleWritable() {}

	public StringTripleWritable(String first, String second, String third) {
		set(first, second);
		this.third = third;
	}

	public void readFields(DataInput in) throws IOException {
		leftElement = Text.readString(in);
		rightElement = Text.readString(in);
		third = Text.readString(in);
	}

	public void write(DataOutput out) throws IOException {
		Text.writeString(out, leftElement);
		Text.writeString(out, rightElement);
		Text.writeString(out, third);
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
	 * Returns the third element.
	 *
	 * @return the right element
	 */
	public String getThirdElement() {
		return third;
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

	public boolean equals(Object obj) {
		if (obj == this) return true;
		else if (obj == null || !(obj instanceof StringPairWritable)) return false;
		StringPairWritable pair = (StringPairWritable) obj;
		boolean res = leftElement.equals(pair.getLeftElement())
				&& rightElement.equals(pair.getRightElement());
		if (!res) return res;
		if (!(obj instanceof StringTripleWritable)) return false;
		StringTripleWritable triple = (StringTripleWritable)obj;
		return ((third == null && triple.third == null) ||
				(third != null && third.equals(triple.third)));
	}

	public int hashCode() {
		return leftElement.hashCode() + rightElement.hashCode() + third.hashCode();
	}

	@Override
	public int compareTo(StringTripleWritable t) {
		String pl = t.getLeftElement();
		String pr = t.getRightElement();
		String pt = t.getThirdElement();

		if (leftElement.equals(pl)) {
			if (rightElement.equals(pr)) {
				return third.compareTo(pt);
			}
			else return rightElement.compareTo(pr);
		}
		else return leftElement.compareTo(pl);
	}

	public StringTripleWritable clone() {
		return new StringTripleWritable(leftElement, rightElement, third);
	}

	/** Comparator optimized for <code>StringPairWritable</code>. */
	public static class Comparator extends WritableComparator {

		/**
		 * Creates a new Comparator optimized for <code>StringPairWritable</code>.
		 */
		public Comparator() {
			super(StringTripleWritable.class);
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
				
				cmp = compareBytes(b1, s1 + firstVIntL1 + firstStrL1 + secondVIntL1, secondStrL1, b2,
						s2 + firstVIntL2 + firstStrL2 + secondVIntL2, secondStrL2);
				if (cmp != 0) {
					return cmp;
				}
				int thirdVIntL1 = WritableUtils.decodeVIntSize(b1[s1 + firstVIntL1 
				                                                  + firstStrL1 + secondVIntL1 + secondStrL1]);
				int thirdVIntL2 = WritableUtils.decodeVIntSize(b2[s2 + firstVIntL2 
				                                                  + firstStrL2 + secondVIntL2 + secondStrL2]);
				
				int thirdStrL1 = readVInt(b1, s1 + firstVIntL1 + firstStrL1 + secondVIntL1 + secondStrL1);
				int thirdStrL2 = readVInt(b2, s2 + firstVIntL2 + firstStrL2 + secondVIntL2 + secondStrL2);
				
				return compareBytes(b1, s1 + firstVIntL1 + firstStrL1 
						+ secondVIntL1 + secondStrL1 + thirdVIntL1, thirdStrL1, b2,
						s2 + firstVIntL2 + firstStrL2 + secondVIntL2 +thirdVIntL2, thirdStrL2);
				
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}


		static { // register this comparator
			WritableComparator.define(StringTripleWritable.class, new Comparator());
		}
	}
}