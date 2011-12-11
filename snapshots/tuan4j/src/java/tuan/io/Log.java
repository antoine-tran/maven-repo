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

import tuan.calendar.Calendar;

/**
 * Light-weigh and fast logging methods
 * 
 * @author tuan
 *
 */
public class Log {
		
	/** Print the logging message to the standard output, prepending a 
	 * current timestamp to it. */
	public static void log(String msg ){
		System.out.println(Calendar.currentDate() + ": " + msg);		
	}
}