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
package tuan.testsuite;

/**
 * This module implements basic settings for a typical experiments in Java
 * @author tuan
 *
 */
public class Environment {
	
	/** set a threshold in a test */
	public static void setThreshold(ThresholdSetable obj, double value) {
		obj.setThreshold(value);
	}
}

