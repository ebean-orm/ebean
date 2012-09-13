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
package com.avaje.tests.xml.runtime;

import java.io.IOException;
import java.io.Writer;

public class XrOutputWriter {

    final Writer writer;
    
    public XrOutputWriter(Writer writer) {
        this.writer = writer;
    }
    
    public void writeXml(Object o){
        
    }
    
    public void write(String s) throws IOException {
        writer.write(s);
    }

    public void writeEncoded(String s) throws IOException {
        writer.write(s);
    }

    int depth = -1;
    
    public void increaseDepth() throws IOException {
        depth += 1;
        indent();
    }

    public void decreaseDepth(boolean indent) throws IOException {
        
        if (indent) {
            indent();
        }
        depth -= 1;
    }

    private void indent() throws IOException  {
        writer.write("\n");
        for (int j = 0; j < depth; j++) {
            writer.write("    ");
        }

    }
}
