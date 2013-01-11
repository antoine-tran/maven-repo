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
	
	public long getKey() {
		return key;
	}

	public int getValue() {
		return val;
	}
	
	public void setKey(long key) {
		this.key = key;
	}
	
	public void setValue(int val) {
		this.val = val;
	}
		
	@Override
	public String toString() {
		return key + "=" + val;
	}
}
