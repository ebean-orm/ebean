/**
 * Copyright (C) 2006  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
