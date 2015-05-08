package com.avaje.ebeaninternal.server.lib.sql;



/**
 * A general DataSource exception.
 */
public class DataSourceException extends RuntimeException
{
    static final long serialVersionUID = 7061559938704539844L;
    
	public DataSourceException(Exception cause) {
		super(cause);
	}
	
    public DataSourceException(String s, Exception cause) {
		super(s, cause);
	}

	public DataSourceException(String s) {
		super(s);
	}

}
