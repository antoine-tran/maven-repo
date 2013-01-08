package tuan.ml.lsh;

import java.util.Iterator;

import javax.annotation.Nullable;

import tuan.ir.Result;
import tuan.ml.data.MetricPoint;

/**
 * An LSH Table is an index of documents based on an LSH algorithm. It
 * does not necessarily fit in main memory
 * @author tuan
 *
 */
public interface LSH<T extends MetricPoint> {

	/** hash a document and put it into the index. Do nothing if the document
	 *  is null */
	public void put(@Nullable T doc);
	
	/** query the document and get its nearest neighbors based on a ranking score */
	public <K> Iterator<? extends Result<K>> neighbours(T query);
	
	/** light-weight query processor that returns only the keys of neighbours in
	 * arbitrary order */
	public Iterable<String> neighbourKeys(T query);
}
