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
package com.avaje.ebeaninternal.server.persist;

import java.sql.SQLException;

/**
 * Handles the processing required after batch execution.
 * <p>
 * This includes concurrency checking, generated keys on inserts, transaction
 * logging, transaction event table modifcation and for beans resetting their
 * 'loaded' status.
 * </p>
 */
public interface BatchPostExecute {


    /**
     * Check that the rowCount is correct for this execute. This is for
     * performing concurrency checking in batch execution.
     */
    public void checkRowCount(int rowCount) throws SQLException;

    /**
     * For inserts with generated keys. Otherwise not used.
     */
    public void setGeneratedKey(Object idValue);

    /**
     * Execute the post execute processing.
     * <p>
     * This includes transaction logging, transaction event table modification
     * and for beans resetting their 'loaded' status.
     * </p>
     */
    public void postExecute() throws SQLException;

}
