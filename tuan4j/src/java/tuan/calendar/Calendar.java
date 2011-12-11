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
package tuan.calendar;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Provide light-weighed methods for accessing and displaying
 * date time information. This class extends and provides
 * unified wrappers for popular date time classes in Java:
 * java.util.Calendar, java.util.Date, java.sql.Date
 *  
 * @author tuan
 *
 */
public class Calendar {
	
	/** The formatter to format the timestamp in loggin messages */
	private static SimpleDateFormat defaultFormat = 
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/** Get the current date time information of the system and display
	 * it using a given timezone. If the timezone input is not specified, then
	 * the default timezone (which is typically not-deterministic) will be 
	 * chosen */
	public static String currentDate(TimeZone timezone) {
		if (timezone == null) currentDate();
		java.util.Calendar calendar = java.util.Calendar.getInstance(timezone);
		defaultFormat.setTimeZone(timezone);
		java.util.Date date = calendar.getTime();
		return defaultFormat.format(date);
	}
	
	/** Get the current date time information of the system and display
	 * it using a default timezone (which is typically not-deterministic) */
	public static String currentDate() {
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		java.util.Date date = calendar.getTime();
		return defaultFormat.format(date);
	}
}
