package tuan.ml.data;

import javax.annotation.Nullable;

/** A metric document is a document defined in high-dimensional metric space */
public interface MetricPoint {

	/** get distance from an arbitrary document */
	public double distance(@Nullable MetricPoint doc);
}
