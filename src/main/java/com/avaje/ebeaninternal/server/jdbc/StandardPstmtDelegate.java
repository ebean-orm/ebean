/**
 * Copyright (C) 2009 Authors
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
package com.avaje.ebeaninternal.server.jdbc;

import java.sql.PreparedStatement;

import com.avaje.ebean.config.PstmtDelegate;
import com.avaje.ebeaninternal.server.lib.sql.ExtendedPreparedStatement;

/**
 * Implementation of PstmtDelegate from Ebean's own
 * DataSource.
 * 
 * @author rbygrave
 */
public class StandardPstmtDelegate implements PstmtDelegate {

	/**
	 * Unwrap the PreparedStatement from Ebean's DataSource implementation.
	 */
	public PreparedStatement unwrap(PreparedStatement pstmt) {
		
		return ((ExtendedPreparedStatement)pstmt).getDelegate(); 
	}
}
