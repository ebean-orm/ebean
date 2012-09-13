package com.avaje.ebeaninternal.server.deploy;

/**
 * A native query defined in deployment xml.
 */
public class DNativeQuery {

	final String query;

	public DNativeQuery(String query) {
		this.query = query;
	}
	
	public String getQuery() {
		return query;
	}
	
}
