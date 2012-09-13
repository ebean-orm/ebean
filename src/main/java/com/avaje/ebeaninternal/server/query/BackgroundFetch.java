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

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebeaninternal.api.SpiTransaction;

/**
 * Continue the fetch using a Background thread. The client knows when this has
 * finished by checking to see if beanList.finishedFetch() is true.
 */
public class BackgroundFetch implements Callable<Integer> {

	private static final Logger logger = Logger.getLogger(BackgroundFetch.class.getName());
	
	private final CQuery<?> cquery;
    
    private final SpiTransaction transaction;
    
    /**
     * Create the BackgroundFetch.
     */
    public BackgroundFetch(CQuery<?> cquery) {
        this.cquery = cquery;  
        this.transaction = cquery.getTransaction();
    }

    /**
     * Continue the fetch.
     */
    public Integer call() {
        try {
            
        	BeanCollection<?> bc = cquery.continueFetchingInBackground();
        	
        	return bc.size();
            
        } catch (Exception e) {
        	logger.log(Level.SEVERE, null, e);
        	return Integer.valueOf(0);
        	
        } finally {
            try {
            	cquery.close();
            } catch (Exception e) {
            	logger.log(Level.SEVERE, null, e);
            }
            try {
            	// we must have our own transaction for background fetching
            	// and this performs the rollback...  returning the 
            	// connection back into the connection pool.
            	transaction.rollback();
            } catch (Exception e) {
            	logger.log(Level.SEVERE, null, e);
            }
        }

    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("BackgroundFetch ").append(cquery);
        return sb.toString();
    }

}
