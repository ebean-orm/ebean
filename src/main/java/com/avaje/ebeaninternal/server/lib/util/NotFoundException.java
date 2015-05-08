package com.avaje.ebeaninternal.server.lib.util;


/**
 * A general exception where data is not found.
 */
public class NotFoundException extends RuntimeException
{
    static final long serialVersionUID = 7061559938704539845L;
    
	public NotFoundException(Exception cause) {
		super(cause);
	}
	
    public NotFoundException(String s, Exception cause) {
		super(s, cause);
	}

	public NotFoundException(String s) {
		super(s);
	}

}
