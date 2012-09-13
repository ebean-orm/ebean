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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import com.avaje.ebean.text.TextException;

public class ReadJsonSourceReader implements ReadJsonSource {

    private final Reader reader;
    
    private char[] localBuffer;
    
    private int totalPos;
    private int localPos;
    private int localPosEnd;

    public ReadJsonSourceReader(Reader reader, int localBufferSize, int bufferSize) {
        this.reader = new BufferedReader(reader,bufferSize);
        this.localBuffer = new char[localBufferSize];
    }
    
    public String toString() {
        return String.valueOf(localBuffer);
    }
    
    
    
    public String getErrorHelp() {
        int prev = localPos - 30;
        if (prev < 0){
            prev = 0;
        }
        String c = new String(localBuffer, prev, (localPos-prev));
        return "pos:"+pos()+" preceding:"+c;
    }

    public int pos() {
        return totalPos+localPos;
    }


    public void ignoreWhiteSpace() {
        do {
            char c = nextChar("EOF ignoring whitespace");
            if (!Character.isWhitespace(c)){
                --localPos;
                break;
            } 
        } while(true);
    }

    public void back() {
        localPos--;
    }
    
    public char nextChar(String eofMsg) {
        if (localPos >= localPosEnd){
            if (!loadLocalBuffer()) {
                throw new TextException(eofMsg+" at pos:"+(totalPos+localPos));
            }
        }
        return localBuffer[localPos++];
    }
        
    private boolean loadLocalBuffer() {
        try {
            localPosEnd = reader.read(localBuffer);
            if (localPosEnd > 0){
                totalPos += localPos;
                localPos = 0;
                return true;
            } else {
                this.localBuffer = null;
                return false;
            }
            
        } catch (IOException e){
            throw new TextException(e);
        }
    }
}
