package com.avaje.ebeaninternal.server.query;

/**
 * Constants used in find processing.
 */
public interface Constants {

	/**
	 * literal used for SQL LIMIT in MySql and Postgres.
	 */
	public static final String LIMIT = "limit";

	/**
	 * Literal used for SQL LIMIT OFFSET clause in MySql and Postgres.
	 */
	public static final String OFFSET = "offset";
	
	/**
	 * ROW_NUMBER() OVER (ORDER BY 
	 */
	public static final String ROW_NUMBER_OVER = "row_number() over (order by ";
	
	/**
	 * ) as rn, 
	 */
	public static final String ROW_NUMBER_AS = ") as rn, ";
}
