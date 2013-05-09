package tuan.ir;

import java.util.Iterator;

import tuan.collections.DoubleObjectTreeMap;
import tuan.collections.DoubleObjectTreeMap.DoubleObjectEntry;

/**
 * An iterator that is backed by a tree map
 */
public class  TreeResultIterator<T> implements Iterator<Result<T>> {

	private DoubleObjectTreeMap<T> treeMap;
	private boolean desc = true;
	
	public TreeResultIterator(DoubleObjectTreeMap<T> map) {
		this.treeMap = map;
	}
	
	public TreeResultIterator(DoubleObjectTreeMap<T> map, boolean des) {
		this.treeMap = map;
		this.desc = des;
	}
	
	@Override
	public boolean hasNext() {
		return treeMap.isEmpty();
	}

	@SuppressWarnings("serial")
	@Override
	public Result<T> next() {
		final DoubleObjectEntry<T> entry = 
				(desc) ? treeMap.pollLastEntry() : treeMap.pollFirstEntry();
		return new Result<T>() {
			public T item() {
				return entry.getValue();
			}
			public double score() {
				return entry.getKey();
			}
		};
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"This tree map does not support remove()");
	}

}
