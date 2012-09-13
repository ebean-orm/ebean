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
import java.util.ArrayList;

import javax.persistence.PersistenceException;

import com.avaje.ebean.QueryIterator;
import com.avaje.ebeaninternal.server.core.OrmQueryRequest;

/**
 * A QueryIterator that uses a buffer to execute secondary queries periodically.
 * 
 * @author rbygrave
 */
class CQueryIteratorWithBuffer<T> implements QueryIterator<T> {

    private final CQuery<T> cquery;
    private final int bufferSize;
    private final OrmQueryRequest<T> request;
    private final ArrayList<T> buffer;

    private boolean moreToLoad = true;

    CQueryIteratorWithBuffer(CQuery<T> cquery, OrmQueryRequest<T> request, int bufferSize) {
        this.cquery = cquery;
        this.request = request;
        this.bufferSize = bufferSize;
        this.buffer = new ArrayList<T>(bufferSize);
    }

    public boolean hasNext() {
        try {
            if (buffer.isEmpty() && moreToLoad) {
                // load buffer
                int i = -1;
                while (moreToLoad && ++i < bufferSize) {
                    if (cquery.hasNextBean(true)) {
                        buffer.add(cquery.getLoadedBean());
                    } else {
                        moreToLoad = false;
                    }
                }
                // execute secondary queries
                request.executeSecondaryQueries(bufferSize);
            }
            return !buffer.isEmpty();

        } catch (SQLException e) {
            throw cquery.createPersistenceException(e);
        }
    }

    public T next() {
        return buffer.remove(0);
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