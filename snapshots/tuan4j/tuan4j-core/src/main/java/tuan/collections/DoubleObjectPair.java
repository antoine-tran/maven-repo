package tuan.collections;

import java.io.Serializable;

/** An entry of key-value pair */
public class DoubleObjectPair<T> {
	
	private double key;
	private T val;
	
	public DoubleObjectPair(double key, T value) {
		this.key = key;
		this.val = value;
	}
	
	public double first() {
		return key;
	}

	public T second() {
		return val;
	}
	
	public void setFirst(double key) {
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
		if (!(obj instanceof DoubleObjectPair<?>)) return false;
		DoubleObjectPair<T> iop = (DoubleObjectPair<T>)obj;
		return ((key == iop.key) && 
				((val == null && iop.val == null) || val.equals(iop.val)));
	}
	
	@Override
	public int hashCode() {
		return (val == null) ? (int)key : (int)key * 31 + val.hashCode();
	}
}
