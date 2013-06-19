package com.avaje.ebeaninternal.server.text.json;

import com.avaje.ebean.text.TextException;

public class ReadBasicJsonContext implements ReadJsonInterface {
    
    private final ReadJsonSource src;
    
    private char tokenStart;
    private String tokenKey;
    private boolean pushedTokenKey;
    
    public ReadBasicJsonContext(ReadJsonSource src) {
        this.src = src;
    }

    /**
     * Push the current token key back onto the 'stack'.
     */
    public void pushTokenKey() {
      pushedTokenKey = true;
    }
    
    public char getToken() {
        return tokenStart;
    }

    public String getTokenKey() {
        return tokenKey;
    }
    
    public boolean isTokenKey() {
        return '\"' == tokenStart;
    }

    public boolean isTokenObjectEnd() {
        return '}' == tokenStart;
    }
        
    public boolean readObjectBegin() {
        readNextToken();
        if ('{' == tokenStart){
            return true;
        } else if ('n' == tokenStart) {
            return false;
        } else if (']' == tokenStart) {
            // an empty array
            return false;
        }
        throw new RuntimeException("Expected object begin at "+src.getErrorHelp());
    }
 
    public boolean readKeyNext() {
        readNextToken();
        if ('\"' == tokenStart){
            return true;
        } else if ('}' == tokenStart) {
            return false;
        }
        throw new RuntimeException("Expected '\"' or '}' at "+src.getErrorHelp());        
    }
    
    public boolean readValueNext() {
        readNextToken();
        if (',' == tokenStart){
            return true;
        } else if ('}' == tokenStart) {
            return false;
        }
        throw new RuntimeException("Expected ',' or '}' at "+src.getErrorHelp()+" but got "+tokenStart);        
    }
    
    public boolean readArrayBegin() {
        readNextToken();
        if ('[' == tokenStart){
            return true;
        } else if ('n' == tokenStart) {
            return false;
        }
        throw new RuntimeException("Expected array begin at "+src.getErrorHelp());
    }
    
    public boolean readArrayNext() {
        readNextToken();
        if (',' == tokenStart){
            return true;
        }
        if (']' == tokenStart){
            return false;
        }
        throw new RuntimeException("Expected ',' or ']' at "+src.getErrorHelp());
    }
    
    public void readNextToken() {
      
      if (pushedTokenKey) {
        // Do nothing 
        pushedTokenKey = false;
        return;
      }
      
        ignoreWhiteSpace();
        
        tokenStart = src.nextChar("EOF finding next token");
        switch (tokenStart) {
        case '"': 
            internalReadKey();
            break;
        case '{': break;
        case '}': break;
        case '[': break; // not expected
        case ']': break; // not expected
        case ',': break; // not expected
        case ':': break; // not expected
        case 'n': 
            internalReadNull();
            break; // not expected

        default:
            throw new RuntimeException("Unexpected tokenStart["+tokenStart+"] "+src.getErrorHelp());
        }
        
    }
    
    public String readQuotedValue() {
        
        boolean escape = false;
        StringBuilder sb = new StringBuilder();

        do {
            char ch = src.nextChar("EOF reading quoted value");
            if (escape) {
                // in escape mode so just append the character
                escape = false;
            	switch (ch) {
                case 'n':
                	sb.append('\n');
                	break;
                case 'r':
                	sb.append('\r');
                	break;
                case 't':
                	sb.append('\t');
                	break;
                case 'f':
                	sb.append('\f');
                	break;
                case 'b':
                	sb.append('\b');
                	break;
                case '"':
                	sb.append('"');
                	break;
                case 'u':
                	String msg = "EOF reading unicode value";
                	char c1 = src.nextChar(msg);
                	char c2 = src.nextChar(msg);
                	char c3 = src.nextChar(msg);
                	char c4 = src.nextChar(msg);
                	char u = (char) Integer.parseInt(""+c1+c2+c3+c4, 16);
                	sb.append(u);
                	break;

                default:
                    sb.append('\\');
                    sb.append(ch);
	                break;
                }
                
            } else {
                switch (ch) {
                case '\\':
                    // put into 'escape' mode for next character
                    escape = true;
                    break;
                case '"':
                    return sb.toString();

                default:
                    sb.append(ch);
                }
            }
        } while (true);
    }

    public String readUnquotedValue(char c) {
        String v = readUnquotedValueRaw(c);
        if ("null".equals(v)){
            return null;
        } else {
            return v;
        }
    }
    
    private String readUnquotedValueRaw(char c) {

        StringBuilder sb = new StringBuilder();
        sb.append(c);
        
        do {
            tokenStart = src.nextChar("EOF reading unquoted value");
            switch (tokenStart) {
            case ',':
                src.back();
                return sb.toString();
                
            case '}':
                src.back();
                return sb.toString();
                
            case ' ':
                return sb.toString();

            case '\t':
                return sb.toString();

            case '\r':
                return sb.toString();
            
            case '\n':
                return sb.toString();

            default:
                sb.append(tokenStart);
            }
            
        } while (true);
        
    }

    private void internalReadNull() {
        
        StringBuilder sb = new StringBuilder(4);
        sb.append(tokenStart);
        for (int i = 0; i < 3; i++) {
            char c = src.nextChar("EOF reading null ");
            sb.append(c);
        }
        if (!"null".equals(sb.toString())){
            throw new TextException("Expected 'null' but got "+sb.toString()+" "+src.getErrorHelp());
        }
    }
    
    private void internalReadKey() {
        StringBuilder sb = new StringBuilder();
        do {
            char c = src.nextChar("EOF reading key");
            if ('\"' == c){
                tokenKey = sb.toString();
                break;
            } else {
                sb.append(c);
            }
        } while (true);
        
        ignoreWhiteSpace();
        
        char c = src.nextChar("EOF reading ':'");
        if (':' != c){
            throw new TextException("Expected to find colon after key at "+(src.pos()-1)+" but found ["+c+"]"+src.getErrorHelp());
        }
    }
    
    public void ignoreWhiteSpace() {
        src.ignoreWhiteSpace();
    }

    public char nextChar() {
        tokenStart = src.nextChar("EOF getting nextChar for raw json");
        return tokenStart;
    }

}
