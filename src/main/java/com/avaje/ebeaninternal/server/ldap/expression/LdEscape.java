package com.avaje.ebeaninternal.server.ldap.expression;

public class LdEscape {

    
    public static String forLike(String source) {
    	
    	// not escaping '*' to "\\\\2a"
    	
    	StringBuilder sb = new StringBuilder(source.length()+5);
    	
    	int len = source.length();
    	for (int i = 0; i < len; i++) {
			
    		char ch = source.charAt(i);
    		switch (ch) {
			case '(':
				sb.append("\\\\28");break;
			case ')':
				sb.append("\\\\29");break;
			case '\\':
				sb.append("\\\\5c");break;
			case '/':
				sb.append("\\\\2f");break;
			case '\0':
				sb.append("\\\\0");break;
			default:
				sb.append(ch);
			}
		}
    	return sb.toString();
    }
}
