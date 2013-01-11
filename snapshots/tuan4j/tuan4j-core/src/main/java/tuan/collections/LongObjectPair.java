package tuan.collections;

import java.io.Serializable;

/** An entry of key-value pair */
@SuppressWarnings("serial")
public class LongObjectPair<T> implements Serializable {
	
	private long key;
	private T val;
	
	public LongObjectPair(long key, T value) {
		this.key = key;
		this.val = value;
	}
	
	public long getKey() {
		return key;
	}

	public T getValue() {
		return val;
	}
	
	public void setKey(long key) {
		this.key = key;
	}
	
	public void setValue(T val) {
		this.val = val;
	}
		
	@Override
	public String toString() {
		return key + "=" + val;
	}
}
