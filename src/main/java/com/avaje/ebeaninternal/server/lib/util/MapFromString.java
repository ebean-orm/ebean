package com.avaje.ebeaninternal.server.lib.util;

import java.util.LinkedHashMap;

/**
 * Utility String class that supports String manipulation functions. 
 */
public class MapFromString {
    
    LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
    
    String mapToString;
        
    int stringLength;
    int keyStart = 0;
    int eqPos = 0;
    int valEnd = 0;
    
    public static LinkedHashMap<String,String> parse(String mapToString) {
        MapFromString c = new MapFromString(mapToString);
        return c.parse();
    }
    
    private MapFromString(String mapToString) {
        if (mapToString.charAt(0) == '{'){
            mapToString = mapToString.substring(1);
        }
        if (mapToString.charAt(mapToString.length()-1) == '}'){
        	mapToString = mapToString.substring(0, mapToString.length()-1);
        }
        
        this.mapToString = mapToString;
        this.stringLength = mapToString.length();
    }
    
    private LinkedHashMap<String,String> parse() {
        while(findNext()){
        }
        return map;
    }
    
    private boolean findNext() {
        if (keyStart > stringLength){
            return false;
        }
        eqPos = mapToString.indexOf("=",keyStart);
        if (eqPos == -1){
            throw new RuntimeException("No = after "+keyStart);
        }
        valEnd = mapToString.indexOf(", ",eqPos);
        if (valEnd == -1){
            valEnd = mapToString.length();
        }
        // check that the next valEnd occurs after the next eqPos
        
        String keyValue = mapToString.substring(keyStart,eqPos);
        String valValue = mapToString.substring(eqPos+1,valEnd);
        map.put(keyValue, valValue);
        keyStart = valEnd + 2;
        return true;
    }

}
