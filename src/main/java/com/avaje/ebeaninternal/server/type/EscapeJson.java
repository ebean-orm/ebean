package com.avaje.ebeaninternal.server.type;

import java.io.IOException;

import com.avaje.ebean.text.TextException;
import com.avaje.ebean.util.StringHelper;

public class EscapeJson {

  /**
   * Unescape the JSON escaped slash character.
   */
  public static String unescapeSlash(String value) {
    return StringHelper.replaceString(value, "\\/","/");
  }
  
	/**
	 * Escape and quote the string value.
	 */
	public static String escapeQuote(String value) {
		if (value == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder(value.length() + 2);
		sb.append("\"");
		escapeAppend(value, sb);
		sb.append("\"");
		return sb.toString();
	}

	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and characters (U+0000 through
	 * U+001F).
	 */
	public static String escape(String s) {
		if (s == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		escapeAppend(s, sb);
		return sb.toString();

	}

	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and characters (U+0000 through
	 * U+001F).
	 */
	public static void escapeAppend(String s, Appendable sb) {

		try {
			for (int i = 0; i < s.length(); i++) {
				char ch = s.charAt(i);
				switch (ch) {
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '/':
					sb.append("\\/");
					break;
				default:
					if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F')
					        || (ch >= '\u2000' && ch <= '\u20FF')) {

						String hs = Integer.toHexString(ch);
						sb.append("\\u");
						for (int j = 0; j < 4 - hs.length(); j++) {
							sb.append('0');
						}
						sb.append(hs.toUpperCase());
					} else {
						sb.append(ch);
					}
				}
			}
		} catch (IOException e) {
			throw new TextException(e);
		}
	}
}
