package com.avaje.ebeaninternal.server.persist;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.SpiUpdate;
import com.avaje.ebeaninternal.server.core.PersistRequestOrmUpdate;
import com.avaje.ebeaninternal.server.core.PstmtBatch;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.util.BindParamsParser;

/**
 * Executes the UpdateSql requests.
 */
public class ExeOrmUpdate {

	private static final Logger logger = Logger.getLogger(ExeOrmUpdate.class.getName());
	
    private final Binder binder;
    
    private final PstmtFactory pstmtFactory;
    
    /**
     * Create with a given binder.
     */
    public ExeOrmUpdate(Binder binder, PstmtBatch pstmtBatch) {
    	this.pstmtFactory = new PstmtFactory(pstmtBatch);
    	this.binder = binder;
    }
    
    /**
     * Execute the orm update request.
     */
    public int execute(PersistRequestOrmUpdate request) {

        SpiTransaction t = request.getTransaction();
        
        boolean batchThisRequest = t.isBatchThisRequest();
        
        PreparedStatement pstmt = null;
        try {
            
        	pstmt = bindStmt(request, batchThisRequest);
        	
            if (batchThisRequest){
                PstmtBatch pstmtBatch = request.getPstmtBatch();
                if (pstmtBatch != null){
                	pstmtBatch.addBatch(pstmt);
                } else {
                	pstmt.addBatch();
                }
                // return -1 to indicate batch mode
                return -1;
                
            } else {
            	SpiUpdate<?> ormUpdate = request.getOrmUpdate();
            	if (ormUpdate.getTimeout() > 0){
            		pstmt.setQueryTimeout(ormUpdate.getTimeout());
            	}
            	
            	int rowCount = pstmt.executeUpdate();
                request.checkRowCount(rowCount);
                request.postExecute();
                return rowCount;
               
            }

        } catch (SQLException ex) {
        	SpiUpdate<?> ormUpdate = request.getOrmUpdate();
        	String msg = "Error executing: "+ormUpdate.getGeneratedSql();
            throw new PersistenceException(msg, ex);

        } finally {
            if (!batchThisRequest && pstmt != null) {
                try {
                	pstmt.close();
                } catch (SQLException e) {
                	logger.log(Level.SEVERE, null, e);
                }
            }
        }
    }
	
    /**
     * Convert bean and property names to db table and columns.
     */
    private String translate(PersistRequestOrmUpdate request, String sql) {
    	
    	BeanDescriptor<?> descriptor = request.getBeanDescriptor();
    	return descriptor.convertOrmUpdateToSql(sql);
    }
	
    private PreparedStatement bindStmt(PersistRequestOrmUpdate request, boolean batchThisRequest) throws SQLException {
        
    	SpiUpdate<?> ormUpdate = request.getOrmUpdate();
    	SpiTransaction t = request.getTransaction();
    	
    	String sql = ormUpdate.getUpdateStatement();
    	
    	// convert bean and property names to table and 
    	// column names if required
    	sql = translate(request, sql);
    	
    	BindParams bindParams = ormUpdate.getBindParams();
        
    	// process named parameters if required
    	sql = BindParamsParser.parse(bindParams, sql);
        
    	ormUpdate.setGeneratedSql(sql);
    	
    	boolean logSql = request.isLogSql();
    	
    	PreparedStatement pstmt;
    	if (batchThisRequest){
    		pstmt = pstmtFactory.getPstmt(t, logSql, sql, request);
    		
    	} else {
    	    if (logSql){
    	        t.logInternal(sql);
    	    }
    		pstmt = pstmtFactory.getPstmt(t, sql);
    	}
        
        String bindLog = null;
        if (!bindParams.isEmpty()){	       
        	bindLog = binder.bind(bindParams, new DataBind(pstmt));
        }
        
        request.setBindLog(bindLog);
        
        return pstmt;
    }

}
