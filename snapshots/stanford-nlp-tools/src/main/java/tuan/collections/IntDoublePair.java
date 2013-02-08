package tuan.collections;

import java.io.Serializable;

public class IntDoublePair implements Serializable {
	
	private int first;
	private double second;
	
	public int first() {
		return first;
	}
	
	public double second() {
		return second;
	}
	
	public IntDoublePair(int f, double s) {
		this.first = f;
		this.second = s;
	}
	
	public void setFirst(int f) {
		this.first = f;
	}
	
	public void setSecond(double s) {
		this.second = s;
	}
	
	@Override
	public String toString() {
		return first + "=" + second;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (!(obj instanceof IntDoublePair)) return false;
		IntDoublePair iop = (IntDoublePair)obj;
		return ((first == iop.first) && (second == iop.second));
	}
	
	@Override
	public int hashCode() {
		return (int) (first * 31 + second);
	}
}
