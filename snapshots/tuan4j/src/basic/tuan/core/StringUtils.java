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
package tuan.core;

import java.io.UnsupportedEncodingException;

/**
 * Provide a list of utility methods for strings
 * @author tuan
 *
 */
public class StringUtils {
	
	/** An efficient method to check if a string is about a number */
	public static boolean isNumeric(String string) {
		return string.matches("-?\\d+(.\\d+)?");
	}

	/***
	 * Calculate the Levenshtein Distance (http://en.wikipedia.org/wiki/Levenshtein_distance)
	 * between two strings. This method was created by Michael Gilleland and then was modified
	 * by Chas Emerick in 22 October 2003, and are publicly available for free 
	 * (http://www.merriampark.com/ldjava.htm)
	 * @param s the first string to be compared
	 * @param t the second string to be compared
	 * @return the integer indicating levenshtein distance between s and t
	 */
	public static int getLevenshteinDistance (String s, String t) {
		if (s == null || t == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}

		/*
		    The difference between this impl. and the previous is that, rather 
		     than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
		     we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
		     is the 'current working' distance array that maintains the newest distance cost
		     counts as we iterate through the characters of String s.  Each time we increment
		     the index of String t we are comparing, d is copied to p, the second int[].  Doing so
		     allows us to retain the previous cost counts as required by the algorithm (taking 
		     the minimum of the cost count to the left, up one, and diagonally up and to the left
		     of the current cost count being calculated).  (Note that the arrays aren't really 
		     copied anymore, just switched...this is clearly much better than cloning an array 
		     or doing a System.arraycopy() each time  through the outer loop.)

		     Effectively, the difference between the two implementations is this one does not 
		     cause an out of memory condition when calculating the LD over two very large strings.  		
		 */		

		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		int p[] = new int[n+1]; //'previous' cost array, horizontally
		int d[] = new int[n+1]; // cost array, horizontally
		int _d[]; //placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for (i = 0; i<=n; i++) {
			p[i] = i;
		}

		for (j = 1; j<=m; j++) {
			t_j = t.charAt(j-1);
			d[0] = j;

			for (i=1; i<=n; i++) {
				cost = s.charAt(i-1)==t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost				
				d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);  
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		} 

		// our last action in the above loop was to switch d and p, so p now 
		// actually has the most recent cost counts
		return p[n];
	}

	/**
	 * Encode a string using Unicode UTF-8 encoding, or null if the encoding cannot be done
	 */
	public static String utf8(String string) {
		try {
			return new String(string.getBytes("UTF-8"), "UTF-8");
		} 
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the literal presentation of utf8 string
	 * @param str
	 * @return
	 */
	public static String utf8Code(String str)
	{
		StringBuilder ostr = new StringBuilder();
		String hex;
		for (int i=0; i<str.length(); i++) {
			char ch = str.charAt(i);
			if ((ch >= 0x0020) && (ch <= 0x007e)) {// Does the char need to be converted to unicode?
				ostr.append(ch);
			} 
			else {
				ostr.append("\\u") ; // standard unicode format.
				hex = Integer.toHexString(ch & 0xFFFF); // Get hex value of the char.
				for(int j=0; j<4-hex.length(); j++) // Prepend zeros because unicode requires 4 digits
					ostr.append("0");
				ostr.append(hex.toLowerCase()); // standard unicode format.
			}
		}
		return ostr.toString(); //Return the stringbuffer cast as a string.
	}
}
