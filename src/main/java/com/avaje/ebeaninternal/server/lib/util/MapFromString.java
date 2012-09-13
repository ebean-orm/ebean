/**
 *  Copyright (C) 2006  Robin Bygrave
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
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
