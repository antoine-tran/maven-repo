package tuan.wikipedia;

import java.io.IOException;

/** java utility class to handle strings in Wikipedia domain 
 * @author tuan */
public class StringUtils {

	/** Tells whether a char is in a range */
	private static boolean in(char c, char a, char b) {
		return (c >= a && c <= b);
	}

	protected static boolean isLowecaseAlphabet(char c) {
		return (in(c, 'a', 'z')); 
	}

	protected static boolean isUppercaseAlphabet(char c) {
		return (in(c, 'A', 'Z')); 
	}

	private static char capitalize(char c) {
		return (char)(c - 32);
	}

	private static char lowercase(char c) {
		return (char)(c + 32);
	}

	/** OpenTSDB only accepts alphanumeric values and "-", "_", ".", "/", but
	 * we will reserve "-" for our encoding purpose */
	public static boolean isUnreserved(char c) {
		return (in(c, 'a', 'z') || in(c, 'A', 'Z') || in(c, '0', '9') || (c == '_'));
	}

	/** encode a char in Wiki format and append it to a buffer */ 
	private static void encodeBackslashAhead(char c, char next, Appendable buffer) 
			throws IOException {
		if (Character.isWhitespace(c)) {
			if (Character.isWhitespace(next) || next == Character.MAX_VALUE) return;
			else buffer.append('_');
		} else {
			String hex = Integer.toHexString(c);
			buffer.append("\\u");
			for (int len = hex.length(); len < 4; len++) {
				buffer.append('0');
			}
			buffer.append(hex);
		}
	}


	/**
	 * take a title (e.g. 'barack obama', and convert to Wikipedia title
	 * (e.g. 'Barack_obama'). The trick is to replace all whitespaces 
	 * with underscores, capitalizing the first character while lowercase
	 * all others (I know, I know, this is a potential gotcha, but we will
	 * use this method for seed articles only most of the time)
	 */
	public static String normalizeWiki(String s) {
		StringBuilder sb = null;
		try {
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (isUnreserved(c)) {
					if (isLowecaseAlphabet(c) && i == 0) {
						c = capitalize(c);
						sb = new StringBuilder(s.length()); // minimum length of a new string
					}				
					else if (isUppercaseAlphabet(c) && i > 0) {
						c = lowercase(c);
						if (sb == null) {
							sb = new StringBuilder(s.length()); // minimum length of a new string
							for (int j = 0; j < i; j++) {
								sb.append(s.charAt(j));
							}
						}
					}
					if (sb != null) sb.append(c); 
				} else {
					if (sb == null) {
						sb = new StringBuilder(s.length() + 3); // minimum length of a new string
						for (int j = 0; j < i; j++) {
							sb.append(s.charAt(j));
						}
					}
					char next = (i < s.length() - 1) ? s.charAt(i + 1) : Character.MAX_VALUE;
					encodeBackslashAhead(c, next, sb);	
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Error encode the tag value " + s, e);
		}
		return (sb != null ? sb.toString() : s);
	}
	
	// Test routine	
	public static void main(String[] args) {
		String s = "saarbücken   oder NIEEE123124 gfdl  t    ";
		System.out.println(normalizeWiki(s));
	}
}
