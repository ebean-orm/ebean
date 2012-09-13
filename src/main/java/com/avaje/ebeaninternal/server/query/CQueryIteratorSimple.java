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
/**
 * 
 */
package com.avaje.ebeaninternal.server.query;

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import com.avaje.ebean.QueryIterator;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;

/**
 * QueryIterator that does not require a buffer for secondary queries.
 * 
 * @author rbygrave
 */
class CQueryIteratorSimple<T> implements QueryIterator<T> {

    private final CQuery<T> cquery;
    private final OrmQueryRequest<T> request;

    CQueryIteratorSimple(CQuery<T> cquery, OrmQueryRequest<T> request){
        this.cquery = cquery;
        this.request = request;
    }
    
    public boolean hasNext() {
        try {
            return cquery.hasNextBean(true);
        } catch (SQLException e){
            throw cquery.createPersistenceException(e);
        }
    }

    public T next() {
        return cquery.getLoadedBean();
    }

    public void close() {
        cquery.updateExecutionStatistics();
        cquery.close();
        request.endTransIfRequired();
    }

    public void remove() {
        throw new PersistenceException("Remove not allowed");
    }
}