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
package tuan.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

/**
 * An interface to wrap a data reader where the constraints can be added
 * on the go. Data are fetch in a push iterator model, and constraints can be
 * updated incrementally or in batches. Most implementations will be backed
 * by an internal iterator or a buffer. Data are transformed directly to
 * the output via push operators, thereby offer a greater flexibility than
 * the traditional Java.util.Iterator<E> interface
 * 
 * @author tuan
 * @since 9.3.1024
 */
public interface DataReader<C, V> extends Closeable {

	/** incrementally update the constraints */
	public void addConstraint(C constraint);
	
	/** Update constraints in batches. Optional method */
	public void addConstraints(Collection<C> constraints);
	
	/** overwrite constraint */
	public void updateConstraint(C constraint);
	
	/** bulk-overwrite constraints */
	public void updateConstraints(Collection<C> constraints);
	
	/** reset the constraint */
	public void reset();
	
	/** initiate the reading */
	public void open() throws IOException;
	
	/** read and push next chunk of data into the buffer both in push and pull manner
	 * Return true if the reading is successful and the buffer
	 * has been updated */
	public V readNext(V value);

	/** check if there is more data to read */
	public boolean hasNext();
	
	/** re-load the constraints and update the subsequent streaming data */
	public void load();
}
