package com.avaje.ebeaninternal.server.el;

/**
 * Case insensitive string matching.
 * <p>
 * Provides an alternative to using regular expressions.
 * </p>
 */
public final class CharMatch {

	private final char[] upperChars;
	
	private final int maxLength;
	
	public CharMatch(String s) {
		this.upperChars = s.toUpperCase().toCharArray();
		this.maxLength = upperChars.length;
	}

	public boolean startsWith(String other) {
		
		if (other == null || other.length() < maxLength){
			return false;
		}
		
		char ta[] = other.toCharArray();
		
		int pos = -1;
		while (++pos < maxLength) {
			char c1 = upperChars[pos];
			char c2 = Character.toUpperCase(ta[pos]);
			if (c1 != c2) {
				return false;
			} 
		}
		return true;
	}
	
	public boolean endsWith(String other) {
		
		if (other == null || other.length() < maxLength){
			return false;
		}
		
		char ta[] = other.toCharArray();
		
		int offset = ta.length - maxLength;
		int pos = maxLength;
		while (--pos >= 0) {
			char c1 = upperChars[pos];
			char c2 = Character.toUpperCase(ta[offset+pos]);
			if (c1 != c2) {
				return false;
			} 
		}
		return true;
	}

}
