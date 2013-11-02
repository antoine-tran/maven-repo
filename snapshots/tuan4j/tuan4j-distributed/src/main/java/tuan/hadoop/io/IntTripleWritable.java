package tuan.hadoop.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class IntTripleWritable implements WritableComparable<IntTripleWritable> {

	private int i1, i2, i3;
	
	/** Create a triple */
	public IntTripleWritable() {
	}
	
	public IntTripleWritable(int i1, int i2, int i3) {
		this.i1 = i1;
		this.i2 = i2;
		this.i3 = i3;
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		this.i1 = in.readInt();
		this.i2 = in.readInt();
		this.i3 = in.readInt();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(i1);
		out.writeInt(i2);
		out.writeInt(i3);
	}

	public int firstElement() {
		return i1;
	}
	
	public int secondElement() {
		return i2;
	}
	
	public int thirdElement() {
		return i3;
	}
	
	public int getKey() {
		return i1;
	}
	
	public IntPairWritable getValue() {
		return new IntPairWritable(i2, i3);
	}
	
	public void set(int i1, int i2, int i3) {
		this.i1 = i1;
		this.i2 = i2;
		this.i3 = i3;
	}
	
	public boolean equals(Object obj) {
		if (obj == this) return true;
		else if (!(obj instanceof IntTripleWritable)) return false;
		IntTripleWritable itw = (IntTripleWritable)obj;
		return (itw.i1 == i1 && itw.i2 == i2 && itw.i3 == i3);
	}
	
	public int hashCode() {
		return (i1 + i2 + i3);
	}
	
	@Override
	public int compareTo(IntTripleWritable t) {
		if (i1 == t.i1) {
			if (i2 == t.i2) {
				if (i3 < t.i3) {
					return -1;
				} 
				if (i3 > t.i3) {
					return 1;
				}
				return 0;
			} 
			if (i2 > t.i2) {
				return 1;
			}
			if (i2 < t.i2) {
				return -1;
			}
		}
		if (i1 > t.i1) {
			return 1;
		}
		return -1;
	}

	public String toString() {
		return "(" + i1 + "," + i2 + "," + i3 + ")";
	}
	
	public IntTripleWritable clone() {
		return new IntTripleWritable(i1, i2, i3);
	}
	
	/** Comparator optimized for <code>IntTripleWritable</code>. */
	public static class Comparator extends WritableComparator {

		/**
		 * Creates a new Comparator optimized for <code>IntTripleWritable</code>.
		 */
		public Comparator() {
			super(IntTripleWritable.class);
		}

		/**
		 * Optimization hook.
		 */
		public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
			int thisi1 = readInt(b1, s1);
			int thati1 = readInt(b2, s2);

			if (thisi1 == thati1) {
				int thisi2 = readInt(b1, s1 + 4);
				int thati2 = readInt(b2, s2 + 4);

				if (thisi2 == thati2) {
					int thisi3 = readInt(b1, s1 + 8);
					int thati3 = readInt(b2, s2 + 8);
					
					return (thisi3 < thati3 ? -1 : (thisi3 == thati3 ? 0 : 1));
				}
				return (thisi2 < thati2 ? -1 : (thisi2 == thati2 ? 0 : 1));
			}

			return (thisi1 < thati1 ? -1 : (thisi1 == thati1 ? 0 : 1));
		}
	}

	static { // register this comparator
		WritableComparator.define(IntTripleWritable.class, new Comparator());
	}
}
