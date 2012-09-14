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
