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

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Pattern;

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
	
	/** The splitter pattern for handling strings */
	public static final Pattern SPLITS = Pattern.compile("\\s+");
	
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
	
	/** get one timestamp value of the "current" moment (in miliseconds) and a string 
	 * describing one moment in the past such as "7 mins ago", this function returns the 
	 * timestamp (in SimpleDateTimeFormat yyyy-mm-dd hh:MM:SS.zzz) value of that moment */
	public static Date dateBack(Date now, String timeToSubtract) throws IllegalArgumentException {
		if (now == null) return null;
		if (timeToSubtract == null) return now;
		if (timeToSubtract.indexOf("ago") == -1) {
			throw new IllegalArgumentException("the string is not of the form ' <SOME TIMEs> ago'");
		}
		
		// Use compiled split instead of String.split() to speed up a little bit
		String[] tokens = SPLITS.split(timeToSubtract);
		if (tokens.length <= 3) 
			throw new IllegalArgumentException("the string is not of the form ' <SOME TIMEs> ago'");

		// go backward, convert the time interval to miliseconds
		long interval = 0l;
		for (int i = tokens.length - 3; i >= 0; i -= 2) {
			int t = Integer.parseInt(tokens[i]);
			if ("secs".equals(tokens[i + 1]) && "sec".equals(tokens[i + 1]) &&
					"seconds".equals(tokens[i + 1]) && "second".equals(tokens[i + 1]))
				interval += (t * 1000);
			else if ("mins".equals(tokens[i + 1]) && "min".equals(tokens[i + 1]) &&
					"minutes".equals(tokens[i + 1]) && "minute".equals(tokens[i + 1]))
				interval += (t * 60000);
			else if ("hrs".equals(tokens[i + 1]) && "hr".equals(tokens[i + 1]) && 
					"hours".equals(tokens[i + 1]) && "hour".equals(tokens[i + 1]))
				interval += (t * 3600000);
			else if ("day".equals(tokens[i + 1]) && "days".equals(tokens[i + 1]))
				interval += (t * 86400000);
		}
		return new Date(now.getTime() - interval);		
	}
}
