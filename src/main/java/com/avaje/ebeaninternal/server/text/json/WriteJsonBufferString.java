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

import java.io.IOException;

public class WriteJsonBufferString implements WriteJsonBuffer {

    private final StringBuilder buffer;
    
    public WriteJsonBufferString(){
        this.buffer = new StringBuilder(256);
    }
    
    public WriteJsonBufferString append(CharSequence csq) throws IOException {
    	buffer.append(csq);
	    return this;
    }

    public WriteJsonBufferString append(CharSequence csq, int start, int end) throws IOException {
		buffer.append(csq, start, end);
	    return this;
    }

    public WriteJsonBufferString append(char c) throws IOException {
    	buffer.append(c);
	    return this;
    }

	public WriteJsonBufferString append(String content){
        buffer.append(content);
        return this;
    }

    public String getBufferOutput() {
        return buffer.toString();
    }
    
    public String toString() {
        return buffer.toString();
    }
}
