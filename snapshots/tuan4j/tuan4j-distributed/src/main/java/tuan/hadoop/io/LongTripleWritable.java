package tuan.hadoop.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

import tuan.hadoop.io.LongTripleWritable.Comparator;

public class LongTripleWritable implements WritableComparable<LongTripleWritable> {

private long i1, i2, i3;
	
	/** Create a triple */
	public LongTripleWritable() {
	}
	
	public LongTripleWritable(long i1, long i2, long i3) {
		this.i1 = i1;
		this.i2 = i2;
		this.i3 = i3;
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		this.i1 = in.readLong();
		this.i2 = in.readLong();
		this.i3 = in.readLong();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeLong(i1);
		out.writeLong(i2);
		out.writeLong(i3);
	}

	public long firstElement() {
		return i1;
	}
	
	public long secondElement() {
		return i2;
	}
	
	public long thirdElement() {
		return i3;
	}
	
	public void set(long i1, long i2, long i3) {
		this.i1 = i1;
		this.i2 = i2;
		this.i3 = i3;
	}
	
	public boolean equals(Object obj) {
		if (obj == this) return true;
		else if (!(obj instanceof LongTripleWritable)) return false;
		LongTripleWritable itw = (LongTripleWritable)obj;
		return (itw.i1 == i1 && itw.i2 == i2 && itw.i3 == i3);
	}
	
	public int hashCode() {
		return (int) (i1 + i2 + i3);
	}
	
	@Override
	public int compareTo(LongTripleWritable t) {
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
	
	public LongTripleWritable clone() {
		return new LongTripleWritable(i1, i2, i3);
	}
	
	/** Comparator optimized for <code>LongTripleWritable</code>. */
	public static class Comparator extends WritableComparator {

		/**
		 * Creates a new Comparator optimized for <code>LongTripleWritable</code>.
		 */
		public Comparator() {
			super(LongTripleWritable.class);
		}

		/**
		 * Optimization hook.
		 */
		public long compare(byte[] b1, int s1, long l1, byte[] b2, int s2, long l2) {
			long thisi1 = readLong(b1, s1);
			long thati1 = readLong(b2, s2);

			if (thisi1 == thati1) {
				long thisi2 = readLong(b1, s1 + 4);
				long thati2 = readLong(b2, s2 + 4);

				if (thisi2 == thati2) {
					long thisi3 = readLong(b1, s1 + 8);
					long thati3 = readLong(b2, s2 + 8);
					
					return (thisi3 < thati3 ? -1 : (thisi3 == thati3 ? 0 : 1));
				}
				return (thisi2 < thati2 ? -1 : (thisi2 == thati2 ? 0 : 1));
			}

			return (thisi1 < thati1 ? -1 : (thisi1 == thati1 ? 0 : 1));
		}
	}

	static { // register this comparator
		WritableComparator.define(LongTripleWritable.class, new Comparator());
	}

}
