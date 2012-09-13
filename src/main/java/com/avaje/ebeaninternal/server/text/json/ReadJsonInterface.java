package com.avaje.ebeaninternal.server.text.json;

public interface ReadJsonInterface {

    public void ignoreWhiteSpace();

	public char nextChar();
	
    public String getTokenKey();
    
	public boolean readKeyNext();

	public boolean readValueNext();

	public boolean readArrayNext();

	public String readQuotedValue();
	
	public String readUnquotedValue(char c);
	

}