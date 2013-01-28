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
	
	public long first() {
		return key;
	}

	public T second() {
		return val;
	}
	
	public void setFirst(long key) {
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
		if (!(obj instanceof LongObjectPair<?>)) return false;
		LongObjectPair<T> iop = (LongObjectPair<T>)obj;
		return ((key == iop.key) && 
				((val == null && iop.val == null) || val.equals(iop.val)));
	}
	
	@Override
	public int hashCode() {
		return (int) ((val == null) ? key : key * 31 + val.hashCode());
	}
}
