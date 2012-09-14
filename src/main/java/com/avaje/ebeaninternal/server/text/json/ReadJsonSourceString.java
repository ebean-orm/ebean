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
