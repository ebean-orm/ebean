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
package com.avaje.ebeaninternal.server.persist.dml;

import java.sql.SQLException;

/**
 * Implementation API for insert update and delete handlers.
 */
public interface PersistHandler {
	
    /**
     * Return the bind log.
     */
    public String getBindLog();

	/**
	 * Get the sql and bind the statement.
	 */
	public void bind() throws SQLException;
    
	/**
	 * Add this for batch execution.
	 */
	public void addBatch() throws SQLException;

	/**
	 * Execute now for non-batch execution.
	 */
    public void execute() throws SQLException;
       
    /**
     * Close resources including underlying preparedStatement.
     */
    public void close() throws SQLException;
}
