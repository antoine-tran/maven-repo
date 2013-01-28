package tuan.collections;

import java.io.Serializable;

/** An entry of key-value pair */
@SuppressWarnings("serial")
public class LongIntPair implements Serializable {
	
	private long key;
	private int val;
	
	public LongIntPair(long key, int value) {
		this.key = key;
		this.val = value;
	}
	
	public long first() {
		return key;
	}

	public int second() {
		return val;
	}
	
	public void setFirst(long key) {
		this.key = key;
	}
	
	public void setSecond(int val) {
		this.val = val;
	}
		
	@Override
	public String toString() {
		return key + "=" + val;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (!(obj instanceof LongIntPair)) return false;
		LongIntPair iop = (LongIntPair)obj;
		return ((key == iop.key) && (val == iop.val));
	}
	
	@Override
	public int hashCode() {
		return (int) (key * 31 + val);
	}
}
