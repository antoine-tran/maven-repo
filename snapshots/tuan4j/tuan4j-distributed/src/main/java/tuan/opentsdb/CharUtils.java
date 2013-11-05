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
		String hex = Integer.toHexString(c);
		buffer.append('-');
		for (int len = hex.length(); len < 4; len++) {
			buffer.append('0');
		}
		buffer.append(hex);
	}

	/** 
	 * Because the Tags class in OpenTSDB only accepts a-z, A-Z, 0-9 and 
	 * some special characters such as , and does not support UTF-8 for 
	 * the moment, this little method makes a manual conversion from UTF-8
	 * characters back to OpenTSDB-acceptable format.
	 * 
	 * Change:
	 * @since 05.11.2013: I changed the code a bit to avoid creating unnecessarily
	 * new objects every time by a lazy instantiation of a new string only in the 
	 * presence of a non-opentsdb-standard character . A worst-case running time
	 * is O(n2), but the amortized analysis would result in much lower value,
	 * and in practice we would not encode a full documents of too many chars...
	 * @throws IOException 
	 */
	public static String encodeTag(String s) throws IllegalArgumentException {
		StringBuilder sb = null;
		try {
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (isUnreserved(c)) {
					if (sb != null) sb.append(c);
				} else {
					if (sb == null) {
						sb = new StringBuilder(s.length() + 3); // minimum length of a new string
						for (int j = 0; j < i; j++) {
							sb.append(s.charAt(j));
						}
					}
					encodeTagChar(c, sb);	
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Error encode the tag value " + s, e);
		}
		return (sb != null ? sb.toString() : s);
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

	/** decode a tag value in OpenTSDB back to UTF-8 Java string. A new string is 
	 * only created if necessary */
	public static String decodeTag(String t) throws IllegalArgumentException {
		int n = t.length();
		StringBuilder sb = null;
		int[] eatenChars = new int[1];
		int offset = 0;
		while (offset < n) {
			char c = extractTagChar(t, offset, eatenChars);
			if (eatenChars[0] == 0) {
				if (sb != null) sb.append(c);
				offset++;
			}
			else {
				if (sb == null) {
					sb = new StringBuilder(n);
					for (int i = 0; i < offset; i++) {
						sb.append(t.charAt(i));
					}
				}
				sb.append(c);
				offset += eatenChars[0];
			}
		}
		return (sb != null ? sb.toString() : t);
	}
}
