package tuan.collections;

import java.io.Serializable;

/** An entry of key-value pair */
@SuppressWarnings("serial")
public class IntObjectPair<T extends Serializable> implements Serializable {
	
	private int key;
	private T val;
	
	public IntObjectPair(int key, T value) {
		this.key = key;
		this.val = value;
	}
	
	public int first() {
		return key;
	}

	public T second() {
		return val;
	}
	
	public void setFirst(int key) {
		this.key = key;
	}
	
	public void setSecond(T val) {
		this.val = val;
	}
		
	@Override
	public String toString() {
		return key + "=" + val;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (!(obj instanceof IntObjectPair<?>)) return false;
		IntObjectPair<T> iop = (IntObjectPair<T>)obj;
		return ((key == iop.key) && 
				((val == null && iop.val == null) || val.equals(iop.val)));
	}
	
	@Override
	public int hashCode() {
		return (val == null) ? key : key * 31 + val.hashCode();
	}
}
