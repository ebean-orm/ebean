package com.avaje.ebeaninternal.server.persist.dml;

import java.sql.SQLException;

/**
 * Implementation API for insert update and delete handlers.
 */
public interface PersistHandler {
	
    /**
     * Return the bind log.
     */
    public String getBindLog();

	/**
	 * Get the sql and bind the statement.
	 */
	public void bind() throws SQLException;
    
	/**
	 * Add this for batch execution.
	 */
	public void addBatch() throws SQLException;

	/**
	 * Execute now for non-batch execution.
	 */
    public void execute() throws SQLException;
       
    /**
     * Close resources including underlying preparedStatement.
     */
    public void close() throws SQLException;
}
