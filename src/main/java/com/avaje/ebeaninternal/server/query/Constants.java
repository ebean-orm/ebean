/**
 * Copyright (C) 2006  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.ebeaninternal.server.query;

/**
 * Constants used in find processing.
 */
public interface Constants {

	/**
	 * the new line character used.
	 * <p>
	 * Note that this is removed for logging sql to the transaction log.
	 * </p>
	 */
	public static final char NEW_LINE = '\n';

	/**
	 * The carriage return character.
	 */
	public static final char CARRIAGE_RETURN = '\r';

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
