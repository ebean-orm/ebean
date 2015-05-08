package com.avaje.ebeaninternal.server.persist;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.avaje.ebeaninternal.server.core.PstmtBatch;

/**
 * A batched statement that is held in BatchedPstmtHolder. It has a list of
 * BatchPostExecute which it will process after the statement is executed.
 * <p>
 * This can hold CallableStatements as well.
 * </p>
 */
public class BatchedPstmt {

    /**
     * The underlying statement.
     */
    private PreparedStatement pstmt;
    
    /**
     * True if an insert that uses generated keys.
     */
    private final boolean isGenKeys;
    
    /**
     * The list of BatchPostExecute used to perform post processing.
     */
    private final ArrayList<BatchPostExecute> list = new ArrayList<BatchPostExecute>();
    
    private final String sql;
    
    private final PstmtBatch pstmtBatch;
    
    private final boolean occCheck;
    
    
    /**
     * Create with a given statement.
     * @param isGenKeys true if an insert that uses generatedKeys
     */
    public BatchedPstmt(PreparedStatement pstmt, boolean isGenKeys, String sql, PstmtBatch pstmtBatch, boolean occCheck) {
    
        this.pstmt = pstmt;
        this.isGenKeys = isGenKeys;
        this.sql = sql;
        this.pstmtBatch = pstmtBatch;
        this.occCheck = occCheck;
    }
    
    /**
     * Return the number of batched statements.
     */
    public int size() {
    	return list.size();
    }
    
    /**
     * Return the sql
     */
    public String getSql() {
    	return sql;
    }

	/**
     * Return the statement.
     */
    public PreparedStatement getStatement() {
        return pstmt;
    }
    
    /**
     * Add the BatchPostExecute to the list for post execute processing.
     */
    public void add(BatchPostExecute batchExecute){
        list.add(batchExecute);
    }
    
    /**
     * Execute the statement using executeBatch().
     * Run any post processing including getGeneratedKeys.
     */
    public void executeBatch(boolean getGeneratedKeys) throws SQLException {
    	
    	executeAndCheckRowCounts();
        if (isGenKeys && getGeneratedKeys){
            getGeneratedKeys();
        }
        postExecute();
        close();
    }
    
    /**
     * Close the underlying statement.
     */
    public void close() throws SQLException {
        if (pstmt != null){
            pstmt.close();
            pstmt = null;
        }
    }
    
    private void postExecute() throws SQLException {
    	for (int i = 0; i < list.size(); i++) {
            list.get(i).postExecute();
        }
    }
    
    private void executeAndCheckRowCounts() throws SQLException {

    	if (pstmtBatch != null){
    		// oracle specific JDBC batch processing
    		int rc = pstmtBatch.executeBatch(pstmt, list.size(), sql, occCheck);
    		if (list.size() == 1){
    			list.get(0).checkRowCount(rc);
    		}
			// the optimistic concurrency row count check
			// has already been done by pstmtBatch so just return
			return;
			
    	}
    	
    	// normal JDBC batch processing
    	int[] results = pstmt.executeBatch();
    	        
        if (results.length != list.size()){
            String s = "results array error "+results.length+" "+list.size();
            throw new SQLException(s);
        }
        
        // check for concurrency exceptions...
        for (int i = 0; i < results.length; i++) {
        	list.get(i).checkRowCount(results[i]);
        }
    }
    
    private void getGeneratedKeys() throws SQLException {
        
        int index = 0;
        ResultSet rset = pstmt.getGeneratedKeys();
        try {
            while(rset.next()) {
                Object idValue = rset.getObject(1);
                list.get(index).setGeneratedKey(idValue);
                index++;
            } 
        } finally {
            if (rset != null){
                rset.close();
            }
        }
    }
    
}
