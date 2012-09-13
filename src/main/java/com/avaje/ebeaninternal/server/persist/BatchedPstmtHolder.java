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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

/**
 * Used to hold BatchedPstmt objects for batch based execution.
 * <p>
 * The BatchControl 'front ends' the batching by queuing the persist requests
 * and ordering them according to depth and type. This object should only batch
 * statements of a single 'depth' at any given time.
 * </p>
 */
public class BatchedPstmtHolder {

	private static final Logger logger = Logger.getLogger(BatchedPstmtHolder.class.getName());
	
	/**
	 * A Map of the statements using a String key. This is used so that the same
	 * Statement,Prepared,Callable is reused.
	 */
	private LinkedHashMap<String,BatchedPstmt> stmtMap = new LinkedHashMap<String,BatchedPstmt>();

	/**
	 * The Max size across all the BatchedPstmt.
	 */
	private int maxSize;
	
	public BatchedPstmtHolder() {
		
	}
	
	/**
	 * Return the PreparedStatement if it has already been used in this Batch.
	 * This will return null if no matching PreparedStatement is found.
	 */
	public PreparedStatement getStmt(String stmtKey, BatchPostExecute postExecute) {
		BatchedPstmt bs = stmtMap.get(stmtKey);
		if (bs == null) {
			// the PreparedStatement has need been created
			return null;
		}
		// add the post execute processing for this bean/row
		bs.add(postExecute);
		
		// maintain a max batch size for any given batched stmt.
		// Used to determine when to flush.
		int bsSize = bs.size();
		if (bsSize > maxSize){
			maxSize = bsSize;
		}
		return bs.getStatement();
	}

	/**
	 * Add a new PreparedStatement wrapped in the BatchStatement object.
	 */
	public void addStmt(BatchedPstmt bs, BatchPostExecute postExecute) {
		// add the batch post execute to the statement for POST processing
		bs.add(postExecute);

		// cache so that getStmt() can find it for additional beans/rows
		stmtMap.put(bs.getSql(), bs);
	}

	/**
	 * Return true if the batch has no statements to execute.
	 */
	public boolean isEmpty() {
		return stmtMap.isEmpty();
	}

	/**
	 * Execute all batched PreparedStatements.
	 * 
	 * @param getGeneratedKeys
	 *            if true try to get generated keys for inserts
	 */
	public void flush(boolean getGeneratedKeys) throws PersistenceException {

		SQLException firstError = null;
		String errorSql = null;

		// flag set if something fails. Will not execute
		// but still need to close PreparedStatements.
		boolean isError = false;

		Iterator<BatchedPstmt> it = stmtMap.values().iterator();
		while (it.hasNext()) {
			BatchedPstmt bs = it.next();
			try {
				if (!isError) {
					bs.executeBatch(getGeneratedKeys);
				}
			} catch (SQLException ex) {
	    		SQLException next = ex.getNextException();
	    		while(next != null) {
	    			logger.log(Level.SEVERE, "Next Exception during batch execution", next);
	    			next = next.getNextException();
	    		}
				
				if (firstError == null) {
					firstError = ex;
					errorSql = bs.getSql();
				} else {
		        	logger.log(Level.SEVERE, null, ex);
				}
				isError = true;

			} finally {
				try {
					bs.close();
				} catch (SQLException ex) {
					// error closing PreparedStatement
		        	logger.log(Level.SEVERE, null, ex);
				}
			}
		}

		// clear the batch cache
		stmtMap.clear();
		maxSize = 0;

		if (firstError != null) {
			String msg = "Error when batch flush on sql: "+errorSql;
			throw new PersistenceException(msg, firstError);
		}
	}

	/**
	 * Return the size of the biggest batched statement.
	 * <p>
	 * Used to determine when to flush the batch.
	 * </p>
	 */
	public int getMaxSize() {
		return maxSize;
	}

}
