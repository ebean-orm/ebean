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
