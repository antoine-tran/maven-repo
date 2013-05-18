package edu.stanford.nlp.ling.tokensregex;

import edu.stanford.nlp.ling.tokensregex.MultiWordStringMatcher.MatchType;

/** a wrapper of java.lang.String that extends the notion of equals
 *  by using different pattern-based matching strategies. This class makes
 *  use of Angel Chang's MultiWordStringMatcher in Stanford NLP toolkit 
 *  
 *  @author Tuan Tran
 *  @since 05/11/2013
 *  */

public class MultiWordString implements CharSequence, Comparable<MultiWordString> {

	private String cs;
	private MatchType type = MatchType.LNRM;
	
	// the pattern object is not created until its first time call
	private String regex;
	private int hashCode;
		
	@Override
	public int compareTo(MultiWordString o) {
		return cs.compareTo(o.toString());
	}

	@Override
	public char charAt(int index) {
		return cs.charAt(index);
	}

	@Override
	public int length() {
		return cs.length();
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return cs.subSequence(start, end);
	}
	
	@Override
	public String toString() {
		return cs;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof MultiWordString)) {
			return false;
		}
		if (pattern() == null) {
			return false;
		}
		
		MultiWordString s = (MultiWordString) obj;
		if (s.pattern() == null) {
			return false;
		}
		return pattern().equals(s.pattern());
	}
	
	@Override
	public int hashCode() {
		if (regex == null) {
			pattern();
		}
		return hashCode;
	}

	public MatchType type() {
		return type;
	}
	
	public String pattern() {
		if (regex == null) {
			switch (type) {
			case LNRM:
				regex = MultiWordStringMatcher.getLnrmRegex(cs);
				break;
			
			default:
				return null;
			}
			if (regex == null) {
				throw new RuntimeException("SEVERE ERROR: Unable to generate pattern for string " + cs);
			}
			regex = regex.toUpperCase();
			hashCode = regex.hashCode();
		}
		return regex;
	}
}
