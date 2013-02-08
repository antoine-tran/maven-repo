package tuan.ml;

/** A metric document is a document defined in high-dimensional metric space */
public interface MetricPoint {

	/** get distance from an arbitrary document */
	public double distance(MetricPoint doc);
}
