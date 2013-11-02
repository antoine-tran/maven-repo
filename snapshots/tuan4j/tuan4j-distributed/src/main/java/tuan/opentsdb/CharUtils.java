package tuan.opentsdb;

import java.io.IOException;

/** Utility methods to handle characters in OpenTSDB tables */
public class CharUtils {

	/** Tells whether a char is in a range */
	public static boolean in(char c, char a, char b) {
		return (c >= a && c <= b);
	}

	/** OpenTSDB only accepts alphanumeric values and "-", "_", ".", "/", but
	 * we will reserve "-" for our encoding purpose */
	public static boolean isUnreserved(char c) {
		return (in(c, 'a', 'z') || in(c, 'A', 'Z') || in(c, '0', '9')
				|| (c == '/') || (c == '_') || (c == '.'));
	}

	/** encode a char in TSDB format and append it to a buffer 
	 * @throws IOException */
	private static void encodeTagChar(char c, Appendable buffer) throws IOException {
		if (isUnreserved(c)) buffer.append(c);
		else {
			String hex = Integer.toHexString(c);
			buffer.append('-');
			for (int len = hex.length(); len < 4; len++) {
				buffer.append('0');
			}
			buffer.append(hex);
		}
	}

	/** 
	 * Because the Tags class in OpenTSDB only accepts a-z, A-Z, 0-9 and 
	 * some special characters such as , and does not support UTF-8 for 
	 * the moment, this little method makes a manual conversion from UTF-8
	 * characters back to OpenTSDB-acceptable format 
	 * @throws IOException 
	 */
	public static String encodeTag(String s) throws IllegalArgumentException {
		StringBuilder sb = new StringBuilder(s.length());
		try {
			for (int i = 0; i < s.length(); i++) {
				encodeTagChar(s.charAt(i), sb);		
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Error encode the tag value " + s, e);
		}
		return (sb.toString());
	}

	private static char extractTagChar(String s, int offset, int[] step) 
			throws IllegalArgumentException {
		char c = s.charAt(offset);
		if (c != '-') {
			step[0] = 0;
			return c;
		} else {
			step[0] = 5;
			return (char) Integer.parseInt(s.substring(offset + 1, offset + 5), 16);				
		}
	}

	/** decode a tag value in OpenTSDB back to UTF-8 Java string */
	public static String decodeTag(String t) throws IllegalArgumentException {
		int n = t.length();
		StringBuilder sb = new StringBuilder(n);
		int[] step = new int[1];
		int offset = 0;
		while (offset < n) {
			char c = extractTagChar(t, offset, step);
			sb.append(c);
			offset += ((step[0] > 0) ? step[0] : 1);
		}
		return sb.toString();
	}
}
