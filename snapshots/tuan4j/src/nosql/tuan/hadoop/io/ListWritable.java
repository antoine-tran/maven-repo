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
package tuan.hadoop.io;

import java.util.RandomAccess;
/**
 * This class extends the Java ArrayList to be writable in Hadoop setting.
 * It is an abstract extension, i.e. no data structure for the 
 * array is specified. It has to be implemented further in order to be used    
 * @author tuan
 *
 */ 
public abstract class ListWritable<T> extends CollectionWritable<T> 
		implements RandomAccess {
		
	/**
	 * Stability sort the elements of the array
	 */
	public abstract void sort(boolean ascending);
	
	/** 
	 * Appends data of the other {@link ListWritable} into the tail of
	 * the array */
	public abstract void concat(ListWritable<T> src);
}
