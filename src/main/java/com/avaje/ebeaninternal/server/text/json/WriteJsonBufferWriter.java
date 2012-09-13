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
import java.io.Writer;

import com.avaje.ebean.text.TextException;

public class WriteJsonBufferWriter implements WriteJsonBuffer {

    private final Writer buffer;
    
    public WriteJsonBufferWriter(Writer buffer){
        this.buffer = buffer;
    }
    
    public WriteJsonBufferWriter append(String content){
        try {
            buffer.write(content);
            return this;
        } catch (IOException e) {
            throw new TextException(e);
        }
    }

    public WriteJsonBufferWriter append(CharSequence csq) throws IOException {
        return append(csq, 0, csq.length());
    }

    public WriteJsonBufferWriter append(CharSequence csq, int start, int end) throws IOException {
		for (int i = start; i < end; i++) {
			buffer.append(csq.charAt(i));
        }
		return this;
    }

    public WriteJsonBufferWriter append(char c) throws IOException {
        try {
            buffer.write(c);
            return this;
        } catch (IOException e) {
            throw new TextException(e);
        }
    }

}
