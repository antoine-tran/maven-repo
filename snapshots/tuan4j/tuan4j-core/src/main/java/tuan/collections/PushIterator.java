/**
 * ==================================
 * 
 * Copyright (c) 2010 Anh Tuan Tran
 *
 * URL: http://www.mpi-inf.mpg.de/~attran,
 *          http://www.l3s.de/~ttran
 *
 * Email: tranatuan24@gmail.com
 * ==================================
 * 
 * This source code is provided with AS IF - it does not guarantee the
 * or compatibilities with older or newer version of third-parties. In any
 * cases, if you have problems regarding using libraries delivered with
 * the project, feel free to write to the above email. Also, we would like
 * to get feedbacks from all of you
 */
package tuan.collections;

import java.util.Iterator;

/**
 * Extended Java Iterator interface that can be used as push operator in
 * asynchronous methods. This is part of the push collection library
 * that enables high-performance parallel applications. <p>NOTE: remove()
 * method is NOT supported in any PushIterator classes. A call to
 * this method always throws an UnsupportedOperationException</p>
 * <p>Like a typical push operator, a PushIterator object needs to be
 * activated with open() method before any other operations. A de-activation
 * operation is done by calling close() method, and after this, all access to
 * the object is not permitted.</p>
 * 
 * @author tuan
 * @since 2012-02-01
 * @version 0.0.1
 */
@Deprecated
public interface PushIterator<T> extends Iterator<T> {

	/** activate the iterator */
	public void open();
	
	/** de-activate the iterator */
	public void close();
	
	/** Add one item into the shared buffer */
	public void add(T item);
	
	/** flush all items from an iterator into the shared buffer */
	public void addAll(Iterator<T> items);
}
