/**
 * 
 */
package tuan.collections;

/**
 * A primitive long - long pair
 * @author tuan
 *
 */
public class LongLongPair {
	private long first;
	private long second;
	
	public LongLongPair(long first, long second) {
		this.first = first;
		this.second = second;
	}
	
	public long first() {
		return first;
	}

	public long second() {
		return second;
	}
	
	public void setFirst(long first) {
		this.first = first;
	}
	
	public void setSecond(long second) {
		this.second = second;
	}
		
	@Override
	public String toString() {
		return first + "=" + second;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (!(obj instanceof LongLongPair)) return false;
		LongLongPair iop = (LongLongPair)obj;
		return ((first == iop.first) && (second == iop.second));
	}
	
	@Override
	public int hashCode() {
		return (int) (first * 31 + second);
	}
}
