package com.avaje.ebeaninternal.server.lib.util;


/**
 * A general exception when creating an Object.
 */
public class CreateObjectException extends RuntimeException
{
    static final long serialVersionUID = 7061559938704539736L;
    
	public CreateObjectException(Exception cause) {
		super(cause);
	}
	
    public CreateObjectException(String s, Exception cause) {
		super(s, cause);
	}

	public CreateObjectException(String s) {
		super(s);
	}

}
