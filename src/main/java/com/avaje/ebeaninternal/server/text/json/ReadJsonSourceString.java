/**
 * Copyright (C) 2009 Authors
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
package com.avaje.ebeaninternal.server.text.json;

import com.avaje.ebean.text.TextException;

public class ReadJsonSourceString implements ReadJsonSource {

    private final String source;
    private final int sourceLength;
    private int pos;
    
    public ReadJsonSourceString(String source){
        this.source = source;
        this.sourceLength = source.length();
    }
    
    public String getErrorHelp() {
        int prev = pos - 50;
        if (prev < 0){
            prev = 0;
        }
        String c = source.substring(prev, pos);
        return "pos:"+pos+" precedingcontent:"+c;
    }

    public String toString() {
        return source;
    }
    
    public int pos() {
        return pos;
    }

    public void back() {
        pos--;
    }

    public char nextChar(String eofMsg) { 
        if (pos >= sourceLength){
            throw new TextException(eofMsg+" at pos:"+pos);
        }
        return source.charAt(pos++);
    }
    
    public void ignoreWhiteSpace() {
        do {
            char c = source.charAt(pos);
            if (Character.isWhitespace(c)){
                ++pos;
            } else {
                break;
            }
        } while(true);
    }
}
