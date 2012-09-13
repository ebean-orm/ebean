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
package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;

/**
 * Reads from and binds to database columns.
 * 
 * @author rbygrave
 */
public interface ScalarDataReader<T> {

    /**
     * Read and return the appropriate value from the dataReader.
     */
    public T read(DataReader dataReader) throws SQLException;

    /**
     * Ignore typically by moving the index position.
     */
    public void loadIgnore(DataReader dataReader);

    /**
     * Bind the value to the underlying preparedStatement.
     */
    public void bind(DataBind b, T value) throws SQLException;

    /**
     * Accumulate all the scalar types used by an immutable compound value type.
     */
    public void accumulateScalarTypes(String propName, CtCompoundTypeScalarList list);

}
