package com.avaje.ebeaninternal.server.query;

public class SplitName {

	public static String add(String prefix, String name){
		if (prefix != null){
			return prefix+"."+name;
		} else {
			return name;
		}
	}

	/**
     * Return the number of occurrences of char in name.
     */
	public static int count(char c, String name){
	    
	    int count = 0;
	    for (int i = 0; i < name.length(); i++) {
	        if (c == name.charAt(i)){
	            count++;
	        }
        }
	    return count;
	}
	
	/**
     * Return the parent part of the path.
     */
    public static String parent(String name) {
        if (name == null){
            return null;
        } else {
            String[] s = split(name, true);
            return s[0];
        }
    }

	public static String[] split(String name){
		return split(name, true);
	}
	
	public static String[] splitBegin(String name){
		return split(name, false);
	}
	
	private static String[] split(String name, boolean last){
		
		int pos =  last ? name.lastIndexOf('.') : name.indexOf('.');
		if (pos == -1){
			if (last){
				return new String[]{null, name};
			} else {
				return new String[]{name, null};
			}
		} else {
			String s0 = name.substring(0, pos);
			String s1 = name.substring(pos+1);
			return new String[]{s0,s1};
		}
	}

}
