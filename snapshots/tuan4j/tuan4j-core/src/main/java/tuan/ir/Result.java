package tuan.ir;

import java.io.Serializable;

/** This interface describes a result item for a query */
public interface Result<T> extends Serializable {
	public T item();
	public double score();
}
