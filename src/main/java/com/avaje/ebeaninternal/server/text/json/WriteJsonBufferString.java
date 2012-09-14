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
