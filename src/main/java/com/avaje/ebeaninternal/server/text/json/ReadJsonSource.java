package com.avaje.ebeaninternal.server.text.json;

public interface ReadJsonSource {

    public char nextChar(String eofMsg); 

    public void ignoreWhiteSpace();

    public void back();

    public int pos();

    public String getErrorHelp();

}
