package com.avaje.ebeaninternal.server.lib.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility String class that supports String manipulation functions.
 */
public class StringHelper {

	/**
	 * Parses out a list of Name Value pairs that are delimited together. Will
	 * always return a StringMap. If allNameValuePairs is null, or no name
	 * values can be parsed out an empty StringMap is returned.
	 * 
	 * @param allNameValuePairs
	 *            the entire string to be parsed.
	 * @param listDelimiter
	 *            (typically ';') the delimited between the list
	 * @param nameValueSeparator
	 *            (typically '=') the separator between the name and value
	 */
	public static Map<String, String> delimitedToMap(String allNameValuePairs,
			String listDelimiter, String nameValueSeparator) {

		HashMap<String, String> params = new HashMap<String, String>();
		if ((allNameValuePairs == null) || (allNameValuePairs.isEmpty())) {
			return params;
		}
		// trim off any leading listDelimiter...
		allNameValuePairs = trimFront(allNameValuePairs, listDelimiter);
		return getKeyValue(params, 0, allNameValuePairs, listDelimiter, nameValueSeparator);
	}

	/**
	 * Trims off recurring strings from the front of a string.
	 * 
	 * @param source
	 *            the source string
	 * @param trim
	 *            the string to trim off the front
	 */
	public static String trimFront(String source, String trim) {
		if (source == null) {
			return null;
		}
		if (source.indexOf(trim) == 0) {
			// dp("trim ...");
			return trimFront(source.substring(trim.length()), trim);
		} else {
			return source;
		}
	}

	/**
	 * Return true if the value is null or an empty string.
	 */
	public static boolean isNull(String value) {
    return value == null || value.trim().isEmpty();
  }

	/**
	 * Recursively pulls out the key value pairs from a raw string.
	 */
	private static HashMap<String, String> getKeyValue(HashMap<String, String> map, int pos,
			String allNameValuePairs, String listDelimiter, String nameValueSeparator) {

		if (pos >= allNameValuePairs.length()) {
			// dp("end as "+pos+" >= "+allNameValuePairs.length() );
			return map;
		}

		int equalsPos = allNameValuePairs.indexOf(nameValueSeparator, pos);
		int delimPos = allNameValuePairs.indexOf(listDelimiter, pos);

		if (delimPos == -1) {
			delimPos = allNameValuePairs.length();
		}
		if (equalsPos == -1) {
			// dp("no more equals...");
			return map;
		}
		if (delimPos == (equalsPos + 1)) {
			// dp("Ignoring as nothing between delim and equals...
			// delim:"+delimPos+" eq:"+equalsPos);
			return getKeyValue(map, delimPos + 1, allNameValuePairs, listDelimiter,
					nameValueSeparator);
		}
		if (equalsPos > delimPos) {
			// there is a key without a value?
			String key = allNameValuePairs.substring(pos, delimPos);
			key = key.trim();
			if (!key.isEmpty()) {
				map.put(key, null);
			}
			return getKeyValue(map, delimPos + 1, allNameValuePairs, listDelimiter,
					nameValueSeparator);

		}
		String key = allNameValuePairs.substring(pos, equalsPos);

		if (delimPos > -1) {
			String value = allNameValuePairs.substring(equalsPos + 1, delimPos);
			// dp("cont "+key+","+value+" pos:"+pos+"
			// len:"+allNameValuePairs.length());
			key = key.trim();

			map.put(key, value);
			pos = delimPos + 1;

			// recurse the rest of the values...
			return getKeyValue(map, pos, allNameValuePairs, listDelimiter, nameValueSeparator);
		} else {
			// dp("ERROR: delimPos < 0 ???");
			return map;
		}
	}

	/**
	 * This method takes a String and will replace all occurrences of the match
	 * String with that of the replace String.
	 * 
	 * @param source
	 *            the source string
	 * @param match
	 *            the string used to find a match
	 * @param replace
	 *            the string used to replace match with
	 * @return the source string after the search and replace
	 */
	public static String replaceString(String source, String match, String replace) {
		if (source == null){
			return null;
		}
		if (replace == null){
			return source;
		}
		if (match == null){
		    throw new NullPointerException("match is null?");
		}
		if (match.equals(replace)){
		    return source;
		}
		return replaceString(source, match, replace, 30, 0, source.length());
	}

	/**
	 * Additionally specify the additionalSize to add to the buffer. This will
	 * make the buffer bigger so that it doesn't have to grow when replacement
	 * occurs.
	 */
	public static String replaceString(String source, String match, String replace,
			int additionalSize, int startPos, int endPos) {

		if (source == null){
			return null;
		}
		
		char match0 = match.charAt(0);
		
		int matchLength = match.length();

		if (matchLength == 1 && replace.length() == 1) {
			char replace0 = replace.charAt(0);
			return source.replace(match0, replace0);
		}
		if (matchLength >= replace.length()) {
			additionalSize = 0;
		}


		int sourceLength = source.length();
		int lastMatch = endPos - matchLength;

		StringBuilder sb = new StringBuilder(sourceLength + additionalSize);

		if (startPos > 0) {
			sb.append(source.substring(0, startPos));
		}

		char sourceChar;
		boolean isMatch;
		int sourceMatchPos;

		for (int i = startPos; i < sourceLength; i++) {
			sourceChar = source.charAt(i);
			if (i > lastMatch || sourceChar != match0) {
				sb.append(sourceChar);

			} else {
				// check to see if this is a match
				isMatch = true;
				sourceMatchPos = i;
				
				// check each following character...
				for (int j = 1; j < matchLength; j++) {
					sourceMatchPos++;
					if (source.charAt(sourceMatchPos) != match.charAt(j)) {
						isMatch = false;
						break;
					}
				}
				if (isMatch) {
					i = i + matchLength - 1;
					sb.append(replace);
				} else {
					// was not a match
					sb.append(sourceChar);
				}
			}
		}

		return sb.toString();
	}

	/**
	 * A search and replace with multiple matching strings.
	 * <p>
	 * Useful when converting CRNL CR and NL all to a BR tag for example.
	 * </p>
	 * 
	 * <pre><code>
	 * String[] multi = { &quot;\r\n&quot;, &quot;\r&quot;, &quot;\n&quot; };
	 * content = StringHelper.replaceStringMulti(content, multi, &quot;&lt;br/&gt;&quot;);
	 * </code></pre>
	 */
	public static String replaceStringMulti(String source, String[] match, String replace) {
		return replaceStringMulti(source, match, replace, 30, 0, source.length());
	}

	/**
	 * Additionally specify an additional size estimate for the buffer plus
	 * start and end positions.
	 * <p>
	 * The start and end positions can limit the search and replace. Otherwise
	 * these default to startPos = 0 and endPos = source.length().
	 * </p>
	 */
	public static String replaceStringMulti(String source, String[] match, String replace,
			int additionalSize, int startPos, int endPos) {

		int shortestMatch = match[0].length();

		char[] match0 = new char[match.length];
		for (int i = 0; i < match0.length; i++) {
			match0[i] = match[i].charAt(0);
			if (match[i].length() < shortestMatch) {
				shortestMatch = match[i].length();
			}
		}

		StringBuilder sb = new StringBuilder(source.length() + additionalSize);

		char sourceChar;

		int len = source.length();
		int lastMatch = endPos - shortestMatch;

		if (startPos > 0) {
			sb.append(source.substring(0, startPos));
		}

		int matchCount;

		for (int i = startPos; i < len; i++) {
			sourceChar = source.charAt(i);
			if (i > lastMatch) {
				sb.append(sourceChar);
			} else {
				matchCount = 0;
				for (int k = 0; k < match0.length; k++) {
					if (matchCount == 0 && sourceChar == match0[k]) {
						if (match[k].length() + i <= len) {

							++matchCount;
							int j = 1;
							for (; j < match[k].length(); j++) {
								if (source.charAt(i + j) != match[k].charAt(j)) {
									--matchCount;
									break;
								}
							}
							if (matchCount > 0) {
								i = i + j - 1;
								sb.append(replace);
								break;
							}
						}
					}
				}
				if (matchCount == 0) {
					sb.append(sourceChar);
				}
			}
		}

		return sb.toString();
	}

}
