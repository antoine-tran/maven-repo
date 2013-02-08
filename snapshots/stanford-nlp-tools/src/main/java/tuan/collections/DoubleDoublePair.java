package tuan.collections;

import java.io.Serializable;

public class DoubleDoublePair implements Serializable {

	private double first;
	private double second;
	
	public double first() {
		return first;
	}
	
	public double second() {
		return second;
	}
	
	public void setFirst(double f) {
		this.first = f;
	}
	
	public void setSecond(double d) {
		this.second = d;
	}
	
	public DoubleDoublePair(double f, double s) {
		this.first = f;
		this.second = s;
	}
	
	public DoubleDoublePair() {
	}

	@Override
	public String toString() {
		return first + "=" + second;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (!(obj instanceof DoubleDoublePair)) return false;
		DoubleDoublePair iop = (DoubleDoublePair)obj;
		return ((first == iop.first) && (second == iop.second));
	}
	
	@Override
	public int hashCode() {
		return (int) (first * 31 + second);
	}
	
}
