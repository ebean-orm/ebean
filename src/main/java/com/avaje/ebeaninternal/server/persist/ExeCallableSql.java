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

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.SpiCallableSql;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequestCallableSql;
import com.avaje.ebeaninternal.server.core.PstmtBatch;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.util.BindParamsParser;

/**
 * Handles the execution of CallableSql requests.
 */
public class ExeCallableSql {

	private static final Logger logger = Logger.getLogger(ExeCallableSql.class.getName());
	
    private final Binder binder;
    
    private final PstmtFactory pstmtFactory;
    
    public ExeCallableSql(Binder binder, PstmtBatch pstmtBatch) {
    	this.binder = binder;
    	// no batch support for CallableStatement in Oracle anyway
    	this.pstmtFactory = new PstmtFactory(null);
    }
    
    /**
     * execute the CallableSql requests.
     */
    public int execute(PersistRequestCallableSql request) {

        SpiTransaction t = request.getTransaction();
        
        boolean batchThisRequest = t.isBatchThisRequest();
        
        CallableStatement cstmt = null;
        try {
            
        	cstmt = bindStmt(request, batchThisRequest);
        	
            if (batchThisRequest){
            	cstmt.addBatch();
                // return -1 to indicate batch mode
                return -1;
                
            } else {
            	// handles executeOverride() and also
            	// reading of registered OUT parameters
            	int rowCount = request.executeUpdate();
                request.postExecute();
                return rowCount;
               
            }

        } catch (SQLException ex) {
            throw new PersistenceException(ex);

        } finally {
            if (!batchThisRequest && cstmt != null) {
                try {
                	cstmt.close();
                } catch (SQLException e) {
                	logger.log(Level.SEVERE, null, e);
                }
            }
        }
    }
	
	
    private CallableStatement bindStmt(PersistRequestCallableSql request, boolean batchThisRequest) throws SQLException {
        
    	SpiCallableSql callableSql = request.getCallableSql();
    	SpiTransaction t = request.getTransaction();
    	
    	String sql = callableSql.getSql();
    	
    	BindParams bindParams = callableSql.getBindParams();
        
    	// process named parameters if required
    	sql = BindParamsParser.parse(bindParams, sql);
        
    	boolean logSql = request.isLogSql();
    	
    	CallableStatement cstmt;
    	if (batchThisRequest){
    		cstmt = pstmtFactory.getCstmt(t, logSql, sql, request);
    		
    	} else {
    	    if (logSql){
    	        t.logInternal(sql);
    	    }
    		cstmt = pstmtFactory.getCstmt(t, sql);
    	}
        
    	if (callableSql.getTimeout() > 0){
    		cstmt.setQueryTimeout(callableSql.getTimeout());
    	}
    	
        String bindLog = null;
        if (!bindParams.isEmpty()){
	        bindLog = binder.bind(bindParams, new DataBind(cstmt));
        }
        
        request.setBindLog(bindLog);
        
        // required to read OUT params later
        request.setBound(bindParams, cstmt);
        
        return cstmt;
    }
}
