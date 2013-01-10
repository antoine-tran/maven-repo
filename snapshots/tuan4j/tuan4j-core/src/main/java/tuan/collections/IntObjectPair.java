package tuan.collections;

import java.io.Serializable;

/** An entry of key-value pair */
public class IntObjectPair<T> implements Serializable {
	
	private int key;
	private T val;
	
	public IntObjectPair(int key, T value) {
		this.key = key;
		this.val = value;
	}
	
	public int getKey() {
		return key;
	}

	public T getValue() {
		return val;
	}
	
	public void setKey(int key) {
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
