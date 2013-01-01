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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tuan.collections.ErasureUtils;
import tuan.io.RuntimeIOException;

/**
 * Provide a list of utility methods for strings
 * @author Tuan Tran
 * @author Dan Klein
 * @author Christopher Manning
 * @author Tim Grow (grow@stanford.edu)
 * @author Chris Cox
 * @version 2006/02/03
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
	
	/**
	   * Don't let anyone instantiate this class.
	   */
	  private StringUtils() {
	  }

	  public static final String[] EMPTY_STRING_ARRAY = new String[0];
	  private static final String PROP = "prop";
	  private static final String PROPS = "props";
	  private static final String PROPERTIES = "properties";
	  private static final String ARGS = "args";
	  private static final String ARGUMENTS = "arguments";

	  /**
	   * Say whether this regular expression can be found inside
	   * this String.  This method provides one of the two "missing"
	   * convenience methods for regular expressions in the String class
	   * in JDK1.4.  This is the one you'll want to use all the time if
	   * you're used to Perl.  What were they smoking?
	   *
	   * @param str   String to search for match in
	   * @param regex String to compile as the regular expression
	   * @return Whether the regex can be found in str
	   */
	  public static boolean find(String str, String regex) {
	    return Pattern.compile(regex).matcher(str).find();
	  }

	  /**
	   * Convenience method: a case-insensitive variant of Collection.contains
	   * @param c Collection&lt;String&gt;
	   * @param s String
	   * @return true if s case-insensitively matches a string in c
	   */
	  public static boolean containsIgnoreCase(Collection<String> c, String s) {
	    for (String squote: c) {
	      if (squote.equalsIgnoreCase(s))
	        return true;
	    }
	    return false;
	  }

	  /**
	   * Say whether this regular expression can be found at the beginning of
	   * this String.  This method provides one of the two "missing"
	   * convenience methods for regular expressions in the String class
	   * in JDK1.4.
	   *
	   * @param str   String to search for match at start of
	   * @param regex String to compile as the regular expression
	   * @return Whether the regex can be found at the start of str
	   */
	  public static boolean lookingAt(String str, String regex) {
	    return Pattern.compile(regex).matcher(str).lookingAt();
	  }

	  /**
	   * Takes a string of the form "x1=y1,x2=y2,..." such
	   * that each y is an integer and each x is a key.  A
	   * String[] s is returned such that s[yn]=xn
	   * @param map A string of the form "x1=y1,x2=y2,..." such
	   *     that each y is an integer and each x is a key.
	   * @return  A String[] s is returned such that s[yn]=xn
	   */
	  public static String[] mapStringToArray(String map) {
	    String[] m = map.split("[,;]");
	    int maxIndex = 0;
	    String[] keys = new String[m.length];
	    int[] indices = new int[m.length];
	    for (int i = 0; i < m.length; i++) {
	      int index = m[i].lastIndexOf('=');
	      keys[i] = m[i].substring(0, index);
	      indices[i] = Integer.parseInt(m[i].substring(index + 1));
	      if (indices[i] > maxIndex) {
	        maxIndex = indices[i];
	      }
	    }
	    String[] mapArr = new String[maxIndex + 1];
	    Arrays.fill(mapArr, null);
	    for (int i = 0; i < m.length; i++) {
	      mapArr[indices[i]] = keys[i];
	    }
	    return mapArr;
	  }


	  /**
	   * Takes a string of the form "x1=y1,x2=y2,..." and returns Map
	   * @param map A string of the form "x1=y1,x2=y2,..."
	   * @return  A Map m is returned such that m.get(xn) = yn
	   */
	  public static Map<String, String> mapStringToMap(String map) {
	    String[] m = map.split("[,;]");
	    Map<String, String> res = new HashMap<String, String>();
	    for (String str : m) {
	      int index = str.lastIndexOf('=');
	      String key = str.substring(0, index);
	      String val = str.substring(index + 1);
	      res.put(key.trim(), val.trim());
	    }
	    return res;
	  }

	  public static List<Pattern> regexesToPatterns(Iterable<String> regexes)
	  {
	    List<Pattern> patterns = new ArrayList<Pattern>();
	    for (String regex:regexes) {
	      patterns.add(Pattern.compile(regex));
	    }
	    return patterns;
	  }

	  /**
	   * Given a pattern and a string, returns a list with the values of the
	   * captured groups in the pattern. If the pattern does not match, returns
	   * null. Note that this uses Matcher.find() rather than Matcher.matches().
	   * If str is null, returns null.
	   */
	  public static List<String> regexGroups(Pattern regex, String str) {
	    if (str == null) {
	      return null;
	    }

	    Matcher matcher = regex.matcher(str);
	    if (!matcher.find()) {
	      return null;
	    }

	    List<String> groups = new ArrayList<String>();
	    for (int index = 1; index <= matcher.groupCount(); index++) {
	      groups.add(matcher.group(index));
	    }

	    return groups;
	  }

	  /**
	   * Say whether this regular expression matches
	   * this String.  This method is the same as the String.matches() method,
	   * and is included just to give a call that is parallel to the other
	   * static regex methods in this class.
	   *
	   * @param str   String to search for match at start of
	   * @param regex String to compile as the regular expression
	   * @return Whether the regex matches the whole of this str
	   */
	  public static boolean matches(String str, String regex) {
	    return Pattern.compile(regex).matcher(str).matches();
	  }


	  public static Set<String> stringToSet(String str, String delimiter)
	  {
	    Set<String> ret = null;
	    if (str != null) {
	      String[] fields = str.split(delimiter);
	      ret = new HashSet<String>(fields.length);
	      for (String field:fields) {
	        field = field.trim();
	        ret.add(field);
	      }
	    }
	    return ret;
	  }

	 public static <E> String join(List<? extends E> l, String glue, Function<E,String> toStringFunc, int start, int end) {
	    StringBuilder sb = new StringBuilder();
	    boolean first = true;
	    start = Math.max(start, 0);
	    end = Math.min(end, l.size());
	    for (int i = start; i < end; i++) {
	      if ( ! first) {
	        sb.append(glue);
	      } else {
	        first = false;
	      }
	      sb.append(toStringFunc.apply(l.get(i)));
	    }
	    return sb.toString();
	  }	  

	  /**
	   * Joins each elem in the {@code Collection} with the given glue.
	   * For example, given a list of {@code Integers}, you can create
	   * a comma-separated list by calling {@code join(numbers, ", ")}.
	   */
	  public static <X> String join(Iterable<X> l, String glue) {
	    StringBuilder sb = new StringBuilder();
	    boolean first = true;
	    for (X o : l) {
	      if ( ! first) {
	        sb.append(glue);
	      } else {
	        first = false;
	      }
	      sb.append(o);
	    }
	    return sb.toString();
	  }

	// Omitted; I'm pretty sure this are redundant with the above
	//  /**
	//   * Joins each elem in the List with the given glue. For example, given a
	//   * list
	//   * of Integers, you can create a comma-separated list by calling
	//   * <tt>join(numbers, ", ")</tt>.
	//   */
	//  public static String join(List l, String glue) {
//	    StringBuilder sb = new StringBuilder();
//	    for (int i = 0, sz = l.size(); i < sz; i++) {
//	      if (i > 0) {
//	        sb.append(glue);
//	      }
//	      sb.append(l.get(i).toString());
//	    }
//	    return sb.toString();
	//  }

	  /**
	   * Joins each elem in the array with the given glue. For example, given a
	   * list of ints, you can create a comma-separated list by calling
	   * <code>join(numbers, ", ")</code>.
	   */
	  public static String join(Object[] elements, String glue) {
	    return (join(Arrays.asList(elements), glue));
	  }

	  /**
	   * Joins elems with a space.
	   */
	  public static String join(Iterable<?> l) {
	    return join(l, " ");
	  }

	  /**
	   * Joins elements with a space.
	   */
	  public static String join(Object[] elements) {
	    return (join(elements, " "));
	  }


	  /**
	   * Splits on whitespace (\\s+).
	   * @param s String to split
	   * @return List<String> of split strings
	   */
	  public static List<String> split(String s) {
	    return split(s, "\\s+");
	  }

	  /**
	   * Splits the given string using the given regex as delimiters.
	   * This method is the same as the String.split() method (except it throws
	   * the results in a List),
	   * and is included just to give a call that is parallel to the other
	   * static regex methods in this class.
	   *
	   * @param str   String to split up
	   * @param regex String to compile as the regular expression
	   * @return List of Strings resulting from splitting on the regex
	   */
	  public static List<String> split(String str, String regex) {
	    return (Arrays.asList(str.split(regex)));
	  }


	  /** Split a string into tokens.  Because there is a tokenRegex as well as a
	   *  separatorRegex (unlike for the conventional split), you can do things
	   *  like correctly split quoted strings or parenthesized arguments.
	   *  However, it doesn't do the unquoting of quoted Strings for you.
	   *  An empty String argument is returned at the beginning, if valueRegex
	   *  accepts the empty String and str begins with separatorRegex.
	   *  But str can end with either valueRegex or separatorRegex and this does
	   *  not generate an empty String at the end (indeed, valueRegex need not
	   *  even accept the empty String in this case.  However, if it does accept
	   *  the empty String and there are multiple trailing separators, then
	   *  empty values will be returned.
	   *
	   *  @param str The String to split
	   *  @param valueRegex Must match a token. You may wish to let it match the empty String
	   *  @param separatorRegex Must match a separator
	   *  @return The List of tokens
	   *  @throws IllegalArgumentException if str cannot be tokenized by the two regex
	   */
	  public static List<String> valueSplit(String str, String valueRegex, String separatorRegex) {
	    Pattern vPat = Pattern.compile(valueRegex);
	    Pattern sPat = Pattern.compile(separatorRegex);
	    List<String> ret = new ArrayList<String>();
	    while (str.length() > 0) {
	      Matcher vm = vPat.matcher(str);
	      if (vm.lookingAt()) {
	        ret.add(vm.group());
	        str = str.substring(vm.end());
	        // String got = vm.group();
	        // System.err.println("vmatched " + got + "; now str is " + str);
	      } else {
	        throw new IllegalArgumentException("valueSplit: " + valueRegex + " doesn't match " + str);
	      }
	      if (str.length() > 0) {
	        Matcher sm = sPat.matcher(str);
	        if (sm.lookingAt()) {
	          str = str.substring(sm.end());
	          // String got = sm.group();
	          // System.err.println("smatched " + got + "; now str is " + str);
	        } else {
	          throw new IllegalArgumentException("valueSplit: " + separatorRegex + " doesn't match " + str);
	        }
	      }
	    } // end while
	    return ret;
	  }


	  /**
	   * Return a String of length a minimum of totalChars characters by
	   * padding the input String str at the right end with spaces.
	   * If str is already longer
	   * than totalChars, it is returned unchanged.
	   */
	  public static String pad(String str, int totalChars) {
	    if (str == null) {
	      str = "null";
	    }
	    int slen = str.length();
	    StringBuilder sb = new StringBuilder(str);
	    for (int i = 0; i < totalChars - slen; i++) {
	      sb.append(' ');
	    }
	    return sb.toString();
	  }

	  /**
	   * Pads the toString value of the given Object.
	   */
	  public static String pad(Object obj, int totalChars) {
	    return pad(obj.toString(), totalChars);
	  }


	  /**
	   * Pad or trim so as to produce a string of exactly a certain length.
	   *
	   * @param str The String to be padded or truncated
	   * @param num The desired length
	   */
	  public static String padOrTrim(String str, int num) {
	    if (str == null) {
	      str = "null";
	    }
	    int leng = str.length();
	    if (leng < num) {
	      StringBuilder sb = new StringBuilder(str);
	      for (int i = 0; i < num - leng; i++) {
	        sb.append(' ');
	      }
	      return sb.toString();
	    } else if (leng > num) {
	      return str.substring(0, num);
	    } else {
	      return str;
	    }
	  }

	  /**
	   * Pad or trim so as to produce a string of exactly a certain length.
	   *
	   * @param str The String to be padded or truncated
	   * @param num The desired length
	   */
	  public static String padLeftOrTrim(String str, int num) {
	    if (str == null) {
	      str = "null";
	    }
	    int leng = str.length();
	    if (leng < num) {
	      StringBuilder sb = new StringBuilder();
	      for (int i = 0; i < num - leng; i++) {
	        sb.append(' ');
	      }
	      sb.append(str);
	      return sb.toString();
	    } else if (leng > num) {
	      return str.substring(str.length() - num);
	    } else {
	      return str;
	    }
	  }

	  /**
	   * Pad or trim the toString value of the given Object.
	   */
	  public static String padOrTrim(Object obj, int totalChars) {
	    return padOrTrim(obj.toString(), totalChars);
	  }


	  /**
	   * Pads the given String to the left with the given character to ensure that
	   * it's at least totalChars long.
	   */
	  public static String padLeft(String str, int totalChars, char ch) {
	    if (str == null) {
	      str = "null";
	    }
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0, num = totalChars - str.length(); i < num; i++) {
	      sb.append(ch);
	    }
	    sb.append(str);
	    return sb.toString();
	  }


	  /**
	   * Pads the given String to the left with spaces to ensure that it's
	   * at least totalChars long.
	   */
	  public static String padLeft(String str, int totalChars) {
	    return padLeft(str, totalChars, ' ');
	  }


	  public static String padLeft(Object obj, int totalChars) {
	    return padLeft(obj.toString(), totalChars);
	  }

	  public static String padLeft(int i, int totalChars) {
	    return padLeft(Integer.valueOf(i), totalChars);
	  }

	  public static String padLeft(double d, int totalChars) {
	    return padLeft(new Double(d), totalChars);
	  }

	  /**
	   * Returns s if it's at most maxWidth chars, otherwise chops right side to fit.
	   */
	  public static String trim(String s, int maxWidth) {
	    if (s.length() <= maxWidth) {
	      return (s);
	    }
	    return (s.substring(0, maxWidth));
	  }

	  public static String trim(Object obj, int maxWidth) {
	    return trim(obj.toString(), maxWidth);
	  }

	  public static String repeat(String s, int times) {
	    if (times == 0) {
	      return "";
	    }
	    StringBuilder sb = new StringBuilder(times * s.length());
	    for (int i = 0; i < times; i++) {
	      sb.append(s);
	    }
	    return sb.toString();
	  }

	  public static String repeat(char ch, int times) {
	    if (times == 0) {
	      return "";
	    }
	    StringBuilder sb = new StringBuilder(times);
	    for (int i = 0; i < times; i++) {
	      sb.append(ch);
	    }
	    return sb.toString();
	  }

	  /**
	   * Returns a "clean" version of the given filename in which spaces have
	   * been converted to dashes and all non-alphanumeric chars are underscores.
	   */
	  public static String fileNameClean(String s) {
	    char[] chars = s.toCharArray();
	    StringBuilder sb = new StringBuilder();
	    for (char c : chars) {
	      if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c == '_')) {
	        sb.append(c);
	      } else {
	        if (c == ' ' || c == '-') {
	          sb.append('_');
	        } else {
	          sb.append('x').append((int) c).append('x');
	        }
	      }
	    }
	    return sb.toString();
	  }

	  /**
	   * Returns the index of the <i>n</i>th occurrence of ch in s, or -1
	   * if there are less than n occurrences of ch.
	   */
	  public static int nthIndex(String s, char ch, int n) {
	    int index = 0;
	    for (int i = 0; i < n; i++) {
	      // if we're already at the end of the string,
	      // and we need to find another ch, return -1
	      if (index == s.length() - 1) {
	        return -1;
	      }
	      index = s.indexOf(ch, index + 1);
	      if (index == -1) {
	        return (-1);
	      }
	    }
	    return index;
	  }


	  /**
	   * This returns a string from decimal digit smallestDigit to decimal digit
	   * biggest digit. Smallest digit is labeled 1, and the limits are
	   * inclusive.
	   */
	  public static String truncate(int n, int smallestDigit, int biggestDigit) {
	    int numDigits = biggestDigit - smallestDigit + 1;
	    char[] result = new char[numDigits];
	    for (int j = 1; j < smallestDigit; j++) {
	      n = n / 10;
	    }
	    for (int j = numDigits - 1; j >= 0; j--) {
	      result[j] = Character.forDigit(n % 10, 10);
	      n = n / 10;
	    }
	    return new String(result);
	  }

	  /**
	   * Parses command line arguments into a Map. Arguments of the form
	   * <p/>
	   * -flag1 arg1a arg1b ... arg1m -flag2 -flag3 arg3a ... arg3n
	   * <p/>
	   * will be parsed so that the flag is a key in the Map (including
	   * the hyphen) and its value will be a {@link String}[] containing
	   * the optional arguments (if present).  The non-flag values not
	   * captured as flag arguments are collected into a String[] array
	   * and returned as the value of <code>null</code> in the Map.  In
	   * this invocation, flags cannot take arguments, so all the {@link
	   * String} array values other than the value for <code>null</code>
	   * will be zero-length.
	   *
	   * @param args A command-line arguments array
	   * @return a {@link Map} of flag names to flag argument {@link
	   *         String} arrays.
	   */
	  public static Map<String, String[]> argsToMap(String[] args) {
	    return argsToMap(args, new HashMap<String, Integer>());
	  }

	  /**
	   * Parses command line arguments into a Map. Arguments of the form
	   * <p/>
	   * -flag1 arg1a arg1b ... arg1m -flag2 -flag3 arg3a ... arg3n
	   * <p/>
	   * will be parsed so that the flag is a key in the Map (including
	   * the hyphen) and its value will be a {@link String}[] containing
	   * the optional arguments (if present).  The non-flag values not
	   * captured as flag arguments are collected into a String[] array
	   * and returned as the value of <code>null</code> in the Map.  In
	   * this invocation, the maximum number of arguments for each flag
	   * can be specified as an {@link Integer} value of the appropriate
	   * flag key in the <code>flagsToNumArgs</code> {@link Map}
	   * argument. (By default, flags cannot take arguments.)
	   * <p/>
	   * Example of usage:
	   * <p/>
	   * <code>
	   * Map flagsToNumArgs = new HashMap();
	   * flagsToNumArgs.put("-x",new Integer(2));
	   * flagsToNumArgs.put("-d",new Integer(1));
	   * Map result = argsToMap(args,flagsToNumArgs);
	   * </code>
	   * <p/>
	   * If a given flag appears more than once, the extra args are appended to
	   * the String[] value for that flag.
	   *
	   * @param args           the argument array to be parsed
	   * @param flagsToNumArgs a {@link Map} of flag names to {@link
	   *                       Integer} values specifying the maximum number of
	   *                       allowed arguments for that flag (default 0).
	   * @return a {@link Map} of flag names to flag argument {@link
	   *         String} arrays.
	   */
	  public static Map<String, String[]> argsToMap(String[] args, Map<String, Integer> flagsToNumArgs) {
	    Map<String, String[]> result = new HashMap<String, String[]>();
	    List<String> remainingArgs = new ArrayList<String>();
	    for (int i = 0; i < args.length; i++) {
	      String key = args[i];
	      if (key.charAt(0) == '-') { // found a flag
	        Integer maxFlagArgs = flagsToNumArgs.get(key);
	        int max = maxFlagArgs == null ? 0 : maxFlagArgs.intValue();
	        List<String> flagArgs = new ArrayList<String>();
	        for (int j = 0; j < max && i + 1 < args.length && args[i + 1].charAt(0) != '-'; i++, j++) {
	          flagArgs.add(args[i + 1]);
	        }
	        if (result.containsKey(key)) { // append the second specification into the args.
	          String[] newFlagArg = new String[result.get(key).length + flagsToNumArgs.get(key)];
	          int oldNumArgs = result.get(key).length;
	          System.arraycopy(result.get(key), 0, newFlagArg, 0, oldNumArgs);
	          for (int j = 0; j < flagArgs.size(); j++) {
	            newFlagArg[j + oldNumArgs] = flagArgs.get(j);
	          }
	          result.put(key, newFlagArg);
	        } else {
	          result.put(key, flagArgs.toArray(new String[flagArgs.size()]));
	        }
	      } else {
	        remainingArgs.add(args[i]);
	      }
	    }
	    result.put(null, remainingArgs.toArray(new String[remainingArgs.size()]));
	    return result;
	  }

	  /**
	   * In this version each flag has zero or one argument. It has one argument
	   * if there is a thing following a flag that does not begin with '-'.  See
	   * {@link #argsToProperties(String[], Map)} for full documentation.
	   *
	   * @param args Command line arguments
	   * @return A Properties object representing the arguments.
	   */
	  public static Properties argsToProperties(String[] args) {
	    return argsToProperties(args, Collections.<String,Integer>emptyMap());
	  }

	  /**
	   * Analogous to {@link #argsToMap}.  However, there are several key differences between this method and {@link #argsToMap}:
	   * <ul>
	   * <li> Hyphens are stripped from flag names </li>
	   * <li> Since Properties objects are String to String mappings, the default number of arguments to a flag is
	   * assumed to be 1 and not 0. </li>
	   * <li> Furthermore, the list of arguments not bound to a flag is mapped to the "" property, not null </li>
	   * <li> The special flags "-prop", "-props", or "-properties" will load the property file specified by its argument. </li>
	   * <li> The value for flags without arguments is set to "true" </li>
	   * <li> If a flag has multiple arguments, the value of the property is all
	   * of the arguments joined together with a space (" ") character between
	   * them.</li>
	   * <li> The value strings are trimmed so trailing spaces do not stop you from loading a file</li>
	   * </ul>
	   *
	   * @param args Command line arguments
	   * @param flagsToNumArgs Map of how many arguments flags should have. The keys are without the minus signs.
	   * @return A Properties object representing the arguments.
	   */
	  public static Properties argsToProperties(String[] args, Map<String,Integer> flagsToNumArgs) {
	    Properties result = new Properties();
	    List<String> remainingArgs = new ArrayList<String>();
	    for (int i = 0; i < args.length; i++) {
	      String key = args[i];
	      if (key.length() > 0 && key.charAt(0) == '-') { // found a flag
	        if (key.charAt(1) == '-')
	          key = key.substring(2); // strip off 2 hyphens
	        else
	          key = key.substring(1); // strip off the hyphen

	        Integer maxFlagArgs = flagsToNumArgs.get(key);
	        int max = maxFlagArgs == null ? 1 : maxFlagArgs;
	        int min = maxFlagArgs == null ? 0 : maxFlagArgs;
	        List<String> flagArgs = new ArrayList<String>();
	        // cdm oct 2007: add length check to allow for empty string argument!
	        for (int j = 0; j < max && i + 1 < args.length && (j < min || args[i + 1].length() == 0 || args[i + 1].length() > 0 && args[i + 1].charAt(0) != '-'); i++, j++) {
	          flagArgs.add(args[i + 1]);
	        }
	        if (flagArgs.isEmpty()) {
	          result.setProperty(key, "true");
	        } else {
	          result.setProperty(key, join(flagArgs, " "));
	          if (key.equalsIgnoreCase(PROP) || key.equalsIgnoreCase(PROPS) || key.equalsIgnoreCase(PROPERTIES) || key.equalsIgnoreCase(ARGUMENTS) || key.equalsIgnoreCase(ARGS))
	          {
	            try {
	              InputStream is = new BufferedInputStream(new FileInputStream(result.getProperty(key)));
	              result.remove(key); // location of this line is critical
	              result.load(is);
	              // trim all values
	              for(Object propKey : result.keySet()){
	                String newVal = result.getProperty((String)propKey);
	                result.setProperty((String)propKey,newVal.trim());
	              }
	              is.close();
	            } catch (IOException e) {
	              result.remove(key);
	              System.err.println("argsToProperties could not read properties file: " + result.getProperty(key));
	              throw new RuntimeException(e);
	            }
	          }
	        }
	      } else {
	        remainingArgs.add(args[i]);
	      }
	    }
	    if (!remainingArgs.isEmpty()) {
	      result.setProperty("", join(remainingArgs, " "));
	    }

	    if (result.containsKey(PROP)) {
	      String file = result.getProperty(PROP);
	      result.remove(PROP);
	      Properties toAdd = argsToProperties(new String[]{"-prop", file});
	      for (Enumeration<?> e = toAdd.propertyNames(); e.hasMoreElements(); ) {
	        String key = (String) e.nextElement();
	        String val = toAdd.getProperty(key);
	        if (!result.containsKey(key)) {
	          result.setProperty(key, val);
	        }
	      }
	    }

	    return result;
	  }


	  /**
	   * This method reads in properties listed in a file in the format prop=value, one property per line.
	   * Although <code>Properties.load(InputStream)</code> exists, I implemented this method to trim the lines,
	   * something not implemented in the <code>load()</code> method.
	   * @param filename A properties file to read
	   * @return The corresponding Properties object
	   */
	  public static Properties propFileToProperties(String filename) {
	    Properties result = new Properties();
	    try {
	      InputStream is = new BufferedInputStream(new FileInputStream(filename));
	      result.load(is);
	      // trim all values
	      for (Object propKey : result.keySet()){
	        String newVal = result.getProperty((String)propKey);
	        result.setProperty((String)propKey,newVal.trim());
	      }
	      is.close();
	      return result;
	    } catch (IOException e) {
	      throw new RuntimeIOException("propFileToProperties could not read properties file: " + filename, e);
	    }
	  }

	  /**
	   * This method converts a comma-separated String (with whitespace
	   * optionally allowed after the comma) representing properties
	   * to a Properties object.  Each property is "property=value".  The value
	   * for properties without an explicitly given value is set to "true". This can be used for a 2nd level
	   * of properties, for example, when you have a commandline argument like "-outputOptions style=xml,tags".
	   */
	  public static Properties stringToProperties(String str) {
	    Properties result = new Properties();
	    return stringToProperties(str, result);
	  }

	  /**
	   * This method updates a Properties object based on
	   * a comma-separated String (with whitespace
	   * optionally allowed after the comma) representing properties
	   * to a Properties object.  Each property is "property=value".  The value
	   * for properties without an explicitly given value is set to "true".
	   */
	  public static Properties stringToProperties(String str, Properties props) {
	    String[] propsStr = str.trim().split(",\\s*");
	    for (String term : propsStr) {
	      int divLoc = term.indexOf('=');
	      String key;
	      String value;
	      if (divLoc >= 0) {
	        key = term.substring(0, divLoc).trim();
	        value = term.substring(divLoc + 1).trim();
	      } else {
	        key = term.trim();
	        value = "true";
	      }
	      props.setProperty(key, value);
	    }
	    return props;
	  }

	  /**
	   * If any of the given list of properties are not found, returns the
	   * name of that property.  Otherwise, returns null.
	   */
	  public static String checkRequiredProperties(Properties props,
	                                               String ... requiredProps) {
	    for (String required : requiredProps) {
	      if (props.getProperty(required) == null) {
	        return required;
	      }
	    }
	    return null;
	  }


	  /**
	   * Prints to a file.  If the file already exists, appends if
	   * <code>append=true</code>, and overwrites if <code>append=false</code>.
	   */
	  public static void printToFile(File file, String message, boolean append,
	                                 boolean printLn, String encoding) {
	    PrintWriter pw = null;
	    try {
	      Writer fw;
	      if (encoding != null) {
	        fw = new OutputStreamWriter(new FileOutputStream(file, append),
	                                         encoding);
	      } else {
	        fw = new FileWriter(file, append);
	      }
	      pw = new PrintWriter(fw);
	      if (printLn) {
	        pw.println(message);
	      } else {
	        pw.print(message);
	      }
	    } catch (Exception e) {
	      System.err.println("Exception: in printToFile " + file.getAbsolutePath());
	      e.printStackTrace();
	    } finally {
	      if (pw != null) {
	        pw.flush();
	        pw.close();
	      }
	    }
	  }


	  /**
	   * Prints to a file.  If the file already exists, appends if
	   * <code>append=true</code>, and overwrites if <code>append=false</code>.
	   */
	  public static void printToFileLn(File file, String message, boolean append) {
	    PrintWriter pw = null;
	    try {
	      Writer fw = new FileWriter(file, append);
	      pw = new PrintWriter(fw);
	      pw.println(message);
	    } catch (Exception e) {
	      System.err.println("Exception: in printToFileLn " + file.getAbsolutePath() + ' ' + message);
	      e.printStackTrace();
	    } finally {
	      if (pw != null) {
	        pw.flush();
	        pw.close();
	      }
	    }
	  }

	  /**
	   * Prints to a file.  If the file already exists, appends if
	   * <code>append=true</code>, and overwrites if <code>append=false</code>.
	   */
	  public static void printToFile(File file, String message, boolean append) {
	    PrintWriter pw = null;
	    try {
	      Writer fw = new FileWriter(file, append);
	      pw = new PrintWriter(fw);
	      pw.print(message);
	    } catch (Exception e) {
	      System.err.println("Exception: in printToFile " + file.getAbsolutePath());
	      e.printStackTrace();
	    } finally {
	      if (pw != null) {
	        pw.flush();
	        pw.close();
	      }
	    }
	  }


	  /**
	   * Prints to a file.  If the file does not exist, rewrites the file;
	   * does not append.
	   */
	  public static void printToFile(File file, String message) {
	    printToFile(file, message, false);
	  }

	  /**
	   * Prints to a file.  If the file already exists, appends if
	   * <code>append=true</code>, and overwrites if <code>append=false</code>
	   */
	  public static void printToFile(String filename, String message, boolean append) {
	    printToFile(new File(filename), message, append);
	  }

	  /**
	   * Prints to a file.  If the file already exists, appends if
	   * <code>append=true</code>, and overwrites if <code>append=false</code>
	   */
	  public static void printToFileLn(String filename, String message, boolean append) {
	    printToFileLn(new File(filename), message, append);
	  }


	  /**
	   * Prints to a file.  If the file does not exist, rewrites the file;
	   * does not append.
	   */
	  public static void printToFile(String filename, String message) {
	    printToFile(new File(filename), message, false);
	  }

	  /**
	   * A simpler form of command line argument parsing.
	   * Dan thinks this is highly superior to the overly complexified code that
	   * comes before it.
	   * Parses command line arguments into a Map. Arguments of the form
	   * -flag1 arg1 -flag2 -flag3 arg3
	   * will be parsed so that the flag is a key in the Map (including the hyphen)
	   * and the
	   * optional argument will be its value (if present).
	   *
	   * @return A Map from keys to possible values (String or null)
	   */
	  @SuppressWarnings("unchecked")
	  public static Map<String, String> parseCommandLineArguments(String[] args) {
	    return (Map)parseCommandLineArguments(args, false);
	  }

	  /**
	   * A simpler form of command line argument parsing.
	   * Dan thinks this is highly superior to the overly complexified code that
	   * comes before it.
	   * Parses command line arguments into a Map. Arguments of the form
	   * -flag1 arg1 -flag2 -flag3 arg3
	   * will be parsed so that the flag is a key in the Map (including the hyphen)
	   * and the
	   * optional argument will be its value (if present).
	   * In this version, if the argument is numeric, it will be a Double value
	   * in the map, not a String.
	   *
	   * @return A Map from keys to possible values (String or null)
	   */
	  public static Map<String, Object> parseCommandLineArguments(String[] args, boolean parseNumbers) {
	    Map<String, Object> result = new HashMap<String, Object>();
	    for (int i = 0; i < args.length; i++) {
	      String key = args[i];
	      if (key.charAt(0) == '-') {
	        if (i + 1 < args.length) {
	          String value = args[i + 1];
	          if (value.charAt(0) != '-') {
	            if (parseNumbers) {
	              Object numericValue = value;
	              try {
	                numericValue = Double.parseDouble(value);
	              } catch (NumberFormatException e2) {
	                // ignore
	              }
	              result.put(key, numericValue);
	            } else {
	              result.put(key, value);
	            }
	            i++;
	          } else {
	            result.put(key, null);
	          }
	        } else {
	          result.put(key, null);
	        }
	      }
	    }
	    return result;
	  }

	  public static String stripNonAlphaNumerics(String orig) {
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < orig.length(); i++) {
	      char c = orig.charAt(i);
	      if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
	        sb.append(c);
	      }
	    }
	    return sb.toString();
	  }

	  public static String stripSGML(String orig) {
	      Pattern sgmlPattern = Pattern.compile("<.*?>", Pattern.DOTALL);
	      Matcher sgmlMatcher = sgmlPattern.matcher(orig);
	      return sgmlMatcher.replaceAll("");
	  }

	  public static void printStringOneCharPerLine(String s) {
	    for (int i = 0; i < s.length(); i++) {
	      int c = s.charAt(i);
	      System.out.println(c + " \'" + (char) c + "\' ");
	    }
	  }

	  public static String escapeString(String s, char[] charsToEscape, char escapeChar) {
	    StringBuilder result = new StringBuilder();
	    for (int i = 0; i < s.length(); i++) {
	      char c = s.charAt(i);
	      if (c == escapeChar) {
	        result.append(escapeChar);
	      } else {
	        for (char charToEscape : charsToEscape) {
	          if (c == charToEscape) {
	            result.append(escapeChar);
	            break;
	          }
	        }
	      }
	      result.append(c);
	    }
	    return result.toString();
	  }

	  /**
	   * This function splits the String s into multiple Strings using the
	   * splitChar.  However, it provides a quoting facility: it is possible to
	   * quote strings with the quoteChar.
	   * If the quoteChar occurs within the quotedExpression, it must be prefaced
	   * by the escapeChar
	   *
	   * @param s         The String to split
	   * @param splitChar The character to split on
	   * @param quoteChar The character to quote items with
	   * @param escapeChar The character to escape the quoteChar with
	   * @return An array of Strings that s is split into
	   */
	  public static String[] splitOnCharWithQuoting(String s, char splitChar, char quoteChar, char escapeChar) {
	    List<String> result = new ArrayList<String>();
	    int i = 0;
	    int length = s.length();
	    StringBuilder b = new StringBuilder();
	    while (i < length) {
	      char curr = s.charAt(i);
	      if (curr == splitChar) {
	        // add last buffer
	        if (b.length() > 0) {
	          result.add(b.toString());
	          b = new StringBuilder();
	        }
	        i++;
	      } else if (curr == quoteChar) {
	        // find next instance of quoteChar
	        i++;
	        while (i < length) {
	          curr = s.charAt(i);
	          // mrsmith: changed this condition from
	          // if (curr == escapeChar) {
	          if ((curr == escapeChar) && (i+1 < length) && (s.charAt(i+1) == quoteChar)) {
	            b.append(s.charAt(i + 1));
	            i += 2;
	          } else if (curr == quoteChar) {
	            i++;
	            break; // break this loop
	          } else {
	            b.append(s.charAt(i));
	            i++;
	          }
	        }
	      } else {
	        b.append(curr);
	        i++;
	      }
	    }
	    if (b.length() > 0) {
	      result.add(b.toString());
	    }
	    return result.toArray(new String[result.size()]);
	  }

	 

	  /**
	   * Computes the longest common contiguous substring of s and t.
	   * The LCCS is the longest run of characters that appear consecutively in
	   * both s and t. For instance, the LCCS of "color" and "colour" is 4, because
	   * of "colo".
	   */
	  public static int longestCommonContiguousSubstring(String s, String t) {
	    if (s.length() == 0 || t.length() == 0) {
	      return 0;
	    }
	    int M = s.length();
	    int N = t.length();
	    int[][] d = new int[M + 1][N + 1];
	    for (int j = 0; j <= N; j++) {
	      d[0][j] = 0;
	    }
	    for (int i = 0; i <= M; i++) {
	      d[i][0] = 0;
	    }

	    int max = 0;
	    for (int i = 1; i <= M; i++) {
	      for (int j = 1; j <= N; j++) {
	        if (s.charAt(i - 1) == t.charAt(j - 1)) {
	          d[i][j] = d[i - 1][j - 1] + 1;
	        } else {
	          d[i][j] = 0;
	        }

	        if (d[i][j] > max) {
	          max = d[i][j];
	        }
	      }
	    }
	    // System.err.println("LCCS(" + s + "," + t + ") = " + max);
	    return max;
	  }

	  /**
	   * Computes the WordNet 2.0 POS tag corresponding to the PTB POS tag s.
	   *
	   * @param s a Penn TreeBank POS tag.
	   */
	  public static String pennPOSToWordnetPOS(String s) {
	    if (s.matches("NN|NNP|NNS|NNPS")) {
	      return "noun";
	    }
	    if (s.matches("VB|VBD|VBG|VBN|VBZ|VBP|MD")) {
	      return "verb";
	    }
	    if (s.matches("JJ|JJR|JJS|CD")) {
	      return "adjective";
	    }
	    if (s.matches("RB|RBR|RBS|RP|WRB")) {
	      return "adverb";
	    }
	    return null;
	  }

	  /**
	   * Returns a short class name for an object.
	   * This is the class name stripped of any package name.
	   *
	   * @return The name of the class minus a package name, for example
	   *         <code>ArrayList</code>
	   */
	  public static String getShortClassName(Object o) {
	    String name = o.getClass().getName();
	    int index = name.lastIndexOf('.');
	    if (index >= 0) {
	      name = name.substring(index + 1);
	    }
	    return name;
	  }


	  /**
	   * Converts a tab delimited string into an object with given fields
	   * Requires the object has setXxx functions for the specified fields
	   *
	   * @param objClass Class of object to be created
	   * @param str string to convert
	   * @param delimiterRegex delimiter regular expression
	   * @param fieldNames fieldnames
	   * @param <T> type to return
	   * @return Object created from string
	   */
	  public static <T> T columnStringToObject(Class objClass, String str, String delimiterRegex, String[] fieldNames)
	          throws InstantiationException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException
	  {
	    Pattern delimiterPattern = Pattern.compile(delimiterRegex);
	    return StringUtils.<T>columnStringToObject(objClass, str, delimiterPattern, fieldNames);
	  }

	  /**
	   * Converts a tab delimited string into an object with given fields
	   * Requires the object has public access for the specified fields
	   *
	   * @param objClass Class of object to be created
	   * @param str string to convert
	   * @param delimiterPattern delimiter
	   * @param fieldNames fieldnames
	   * @param <T> type to return
	   * @return Object created from string
	   */
	  public static <T> T columnStringToObject(Class<?> objClass, String str, Pattern delimiterPattern, String[] fieldNames)
	          throws InstantiationException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InvocationTargetException
	  {
	    String[] fields = delimiterPattern.split(str);
	    T item = ErasureUtils.<T>uncheckedCast(objClass.newInstance());
	    for (int i = 0; i < fields.length; i++) {
	      try {
	        Field field = objClass.getDeclaredField(fieldNames[i]);
	        field.set(item, fields[i]);
	      } catch (IllegalAccessException ex) {
	        Method method = objClass.getDeclaredMethod("set" + StringUtils.capitalize(fieldNames[i]), String.class);
	        method.invoke(item, fields[i]);
	      }
	    }
	    return item;
	  }

	  /**
	   * Converts an object into a tab delimited string with given fields
	   * Requires the object has public access for the specified fields
	   *
	   * @param object Object to convert
	   * @param delimiter delimiter
	   * @param fieldNames fieldnames
	   * @return String representing object
	   */
	  public static String objectToColumnString(Object object, String delimiter, String[] fieldNames)
	          throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException
	  {
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < fieldNames.length; i++) {
	      if (sb.length() > 0) {
	        sb.append(delimiter);
	      }
	      try {
	        Field field = object.getClass().getDeclaredField(fieldNames[i]);
	        sb.append(field.get(object)) ;
	      } catch (IllegalAccessException ex) {
	        Method method = object.getClass().getDeclaredMethod("get" + StringUtils.capitalize(fieldNames[i]));
	        sb.append(method.invoke(object));
	      }
	    }
	    return sb.toString();
	  }

	  /**
	   * Uppercases the first character of a string.
	   *
	   * @param s a string to capitalize
	   * @return a capitalized version of the string
	   */
	  public static String capitalize(String s) {
	    if (Character.isLowerCase(s.charAt(0))) {
	      return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	    } else {
	      return s;
	    }
	  }

	  /**
	   * Check if a string begins with an uppercase.
	   *
	   * @param s a string
	   * @return true if the string is capitalized
	   *         false otherwise
	   */
	  public static boolean isCapitalized(String s) {
	    return (Character.isUpperCase(s.charAt(0)));
	  }

	  public static String searchAndReplace(String text, String from, String to) {
	    from = escapeString(from, new char[]{'.', '[', ']', '\\'}, '\\'); // special chars in regex
	    Pattern p = Pattern.compile(from);
	    Matcher m = p.matcher(text);
	    return m.replaceAll(to);
	  }

	  /**
	   * Returns an HTML table containing the matrix of Strings passed in.
	   * The first dimension of the matrix should represent the rows, and the
	   * second dimension the columns.
	   */
	  public static String makeHTMLTable(String[][] table, String[] rowLabels, String[] colLabels) {
	    StringBuilder buff = new StringBuilder();
	    buff.append("<table class=\"auto\" border=\"1\" cellspacing=\"0\">\n");
	    // top row
	    buff.append("<tr>\n");
	    buff.append("<td></td>\n"); // the top left cell
	    for (int j = 0; j < table[0].length; j++) { // assume table is a rectangular matrix
	      buff.append("<td class=\"label\">").append(colLabels[j]).append("</td>\n");
	    }
	    buff.append("</tr>\n");
	    // all other rows
	    for (int i = 0; i < table.length; i++) {
	      // one row
	      buff.append("<tr>\n");
	      buff.append("<td class=\"label\">").append(rowLabels[i]).append("</td>\n");
	      for (int j = 0; j < table[i].length; j++) {
	        buff.append("<td class=\"data\">");
	        buff.append(((table[i][j] != null) ? table[i][j] : ""));
	        buff.append("</td>\n");
	      }
	      buff.append("</tr>\n");
	    }
	    buff.append("</table>");
	    return buff.toString();
	  }

	  /**
	   * Returns an text table containing the matrix of Strings passed in.
	   * The first dimension of the matrix should represent the rows, and the
	   * second dimension the columns.
	   */
	  public static String makeAsciiTable(Object[][] table, Object[] rowLabels, Object[] colLabels, int padLeft, int padRight, boolean tsv) {
	    StringBuilder buff = new StringBuilder();
	    // top row
	    buff.append(makeAsciiTableCell("", padLeft, padRight, tsv)); // the top left cell
	    for (int j = 0; j < table[0].length; j++) { // assume table is a rectangular matrix
	      buff.append(makeAsciiTableCell(colLabels[j], padLeft, padRight, (j != table[0].length - 1) && tsv));
	    }
	    buff.append('\n');
	    // all other rows
	    for (int i = 0; i < table.length; i++) {
	      // one row
	      buff.append(makeAsciiTableCell(rowLabels[i], padLeft, padRight, tsv));
	      for (int j = 0; j < table[i].length; j++) {
	        buff.append(makeAsciiTableCell(table[i][j], padLeft, padRight, (j != table[0].length - 1) && tsv));
	      }
	      buff.append('\n');
	    }
	    return buff.toString();
	  }


	  /** The cell String is the string representation of the object.
	   *  If padLeft is greater than 0, it is padded. Ditto right
	   *
	   */
	  private static String makeAsciiTableCell(Object obj, int padLeft, int padRight, boolean tsv) {
	    String result = obj.toString();
	    if (padLeft > 0) {
	      result = padLeft(result, padLeft);
	    }
	    if (padRight > 0) {
	      result = pad(result, padRight);
	    }
	    if (tsv) {
	      result = result + '\t';
	    }
	    return result;
	  }

	  /**
	   * Tests the string edit distance function.
	   */
	  public static void main(String[] args) {

	    String[] s = {"there once was a man", "this one is a manic", "hey there", "there once was a mane", "once in a manger.", "where is one match?", "Jo3seph Smarr!", "Joseph R Smarr"};
	    for (int i = 0; i < 8; i++) {
	      for (int j = 0; j < 8; j++) {
	        System.out.println("s1: " + s[i]);
	        System.out.println("s2: " + s[j]);
	        System.out.println("LCCS: " + longestCommonContiguousSubstring(s[i], s[j]));
	        System.out.println();
	      }
	    }
	  }

	  public static String toAscii(String s) {
	    StringBuilder b = new StringBuilder();
	    for (int i = 0; i < s.length(); i++) {
	      char c = s.charAt(i);
	      if (c > 127) {
	        String result = "?";
	        if (c >= 0x00c0 && c <= 0x00c5) {
	          result = "A";
	        } else if (c == 0x00c6) {
	          result = "AE";
	        } else if (c == 0x00c7) {
	          result = "C";
	        } else if (c >= 0x00c8 && c <= 0x00cb) {
	          result = "E";
	        } else if (c >= 0x00cc && c <= 0x00cf) {
	          result = "F";
	        } else if (c == 0x00d0) {
	          result = "D";
	        } else if (c == 0x00d1) {
	          result = "N";
	        } else if (c >= 0x00d2 && c <= 0x00d6) {
	          result = "O";
	        } else if (c == 0x00d7) {
	          result = "x";
	        } else if (c == 0x00d8) {
	          result = "O";
	        } else if (c >= 0x00d9 && c <= 0x00dc) {
	          result = "U";
	        } else if (c == 0x00dd) {
	          result = "Y";
	        } else if (c >= 0x00e0 && c <= 0x00e5) {
	          result = "a";
	        } else if (c == 0x00e6) {
	          result = "ae";
	        } else if (c == 0x00e7) {
	          result = "c";
	        } else if (c >= 0x00e8 && c <= 0x00eb) {
	          result = "e";
	        } else if (c >= 0x00ec && c <= 0x00ef) {
	          result = "i";
	        } else if (c == 0x00f1) {
	          result = "n";
	        } else if (c >= 0x00f2 && c <= 0x00f8) {
	          result = "o";
	        } else if (c >= 0x00f9 && c <= 0x00fc) {
	          result = "u";
	        } else if (c >= 0x00fd && c <= 0x00ff) {
	          result = "y";
	        } else if (c >= 0x2018 && c <= 0x2019) {
	          result = "\'";
	        } else if (c >= 0x201c && c <= 0x201e) {
	          result = "\"";
	        } else if (c >= 0x0213 && c <= 0x2014) {
	          result = "-";
	        } else if (c >= 0x00A2 && c <= 0x00A5) {
	          result = "$";
	        } else if (c == 0x2026) {
	          result = ".";
	        }
	        b.append(result);
	      } else {
	        b.append(c);
	      }
	    }
	    return b.toString();
	  }


	  public static String toCSVString(String[] fields) {
	    StringBuilder b = new StringBuilder();
	    for (String fld : fields) {
	      if (b.length() > 0) {
	        b.append(',');
	      }
	      String field = escapeString(fld, new char[]{'\"'}, '\"'); // escape quotes with double quotes
	      b.append('\"').append(field).append('\"');
	    }
	    return b.toString();
	  }

	  /**
	   * Swap any occurrences of any characters in the from String in the input String with
	   * the corresponding character from the to String.  As Perl tr, for example,
	   * tr("chris", "irs", "mop").equals("chomp"), except it does not
	   * support regular expression character ranges.
	   * <p>
	   * <i>Note:</i> This is now optimized to not allocate any objects if the
	   * input is returned unchanged.
	   */
	  public static String tr(String input, String from, String to) {
	    assert from.length() == to.length();
	    StringBuilder sb = null;
	    int len = input.length();
	    for (int i = 0; i < len; i++) {
	      int ind = from.indexOf(input.charAt(i));
	      if (ind >= 0) {
	        if (sb == null) {
	          sb = new StringBuilder(input);
	        }
	        sb.setCharAt(i, to.charAt(ind));
	      }
	    }
	    if (sb == null) {
	      return input;
	    } else {
	      return sb.toString();
	    }
	  }

	  /**
	   * Returns the supplied string with any trailing '\n' removed.
	   */
	  public static String chomp(String s) {
	    if(s.length() == 0)
	      return s;
	    int l_1 = s.length() - 1;
	    if (s.charAt(l_1) == '\n') {
	      return s.substring(0, l_1);
	    }
	    return s;
	  }

	  /**
	   * Returns the result of calling toString() on the supplied Object, but with
	   * any trailing '\n' removed.
	   */
	  public static String chomp(Object o) {
	    return chomp(o.toString());
	  }


	  public static void printErrInvocationString(String cls, String[] args) {
	    System.err.println(toInvocationString(cls, args));
	  }


	  public static String toInvocationString(String cls, String[] args) {
	    StringBuilder sb = new StringBuilder();
	    sb.append(cls).append(" invoked on ").append(new Date());
	    sb.append(" with arguments:\n  ");
	    for (String arg : args) {
	      sb.append(' ').append(arg);
	    }
	    return sb.toString();
	  }

	  /**
	   * Strip directory from filename.  Like Unix 'basename'. <p/>
	   *
	   * Example: <code>getBaseName("/u/wcmac/foo.txt") ==> "foo.txt"</code>
	   */
	  public static String getBaseName(String fileName) {
	    return getBaseName(fileName, "");
	  }

	  /**
	   * Strip directory and suffix from filename.  Like Unix 'basename'. <p/>
	   *
	   * Example: <code>getBaseName("/u/wcmac/foo.txt", "") ==> "foo.txt"</code><br/>
	   * Example: <code>getBaseName("/u/wcmac/foo.txt", ".txt") ==> "foo"</code><br/>
	   * Example: <code>getBaseName("/u/wcmac/foo.txt", ".pdf") ==> "foo.txt"</code><br/>
	   */
	  public static String getBaseName(String fileName, String suffix) {
	    String[] elts = fileName.split("/");
	    String lastElt = elts[elts.length - 1];
	    if (lastElt.endsWith(suffix)) {
	      lastElt = lastElt.substring(0, lastElt.length() - suffix.length());
	    }
	    return lastElt;
	  }

	  /**
	   * Given a String the method uses Regex to check if the String only contains alphabet characters
	   *
	   * @param s a String to check using regex
	   * @return true if the String is valid
	   */
	  public static boolean isAlpha(String s){
	    Pattern p = Pattern.compile("^[\\p{Alpha}\\s]+$");
	    Matcher m = p.matcher(s);
	    return m.matches();
	  }

	  /**
	   * Given a String the method uses Regex to check if the String only contains alphanumeric characters
	   *
	   * @param s a String to check using regex
	   * @return true if the String is valid
	   */
	  public static boolean isAlphanumeric(String s){
	    Pattern p = Pattern.compile("^[\\p{Alnum}\\s\\.]+$");
	    Matcher m = p.matcher(s);
	    return m.matches();
	  }

	  /**
	   * Given a String the method uses Regex to check if the String only contains punctuation characters
	   *
	   * @param s a String to check using regex
	   * @return true if the String is valid
	   */
	  public static boolean isPunct(String s){
	    Pattern p = Pattern.compile("^[\\p{Punct}]+$");
	    Matcher m = p.matcher(s);
	    return m.matches();
	  }

	  /**
	   * Given a String the method uses Regex to check if the String looks like an acronym
	   *
	   * @param s a String to check using regex
	   * @return true if the String is valid
	   */
	  public static boolean isAcronym(String s){
	    Pattern p = Pattern.compile("^[\\p{Upper}]+$");
	    Matcher m = p.matcher(s);
	    return m.matches();
	  }

	  public static String getNotNullString(String s){
		  if(s == null)
			  return "";
		  else
			  return s;
	  }
}
