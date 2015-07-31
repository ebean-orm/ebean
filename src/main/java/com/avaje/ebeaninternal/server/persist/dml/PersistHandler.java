package com.avaje.ebeaninternal.server.persist.dml;

import java.sql.SQLException;

/**
 * Implementation API for insert update and delete handlers.
 */
public interface PersistHandler {
	
    /**
     * Return the bind log.
     */
    String getBindLog();

	/**
	 * Get the sql and bind the statement.
	 */
	void bind() throws SQLException;
    
	/**
	 * Add this for batch execution.
	 */
	void addBatch() throws SQLException;

	/**
	 * Execute now for non-batch execution.
	 */
    void execute() throws SQLException;
       
    /**
     * Close resources including underlying preparedStatement.
     */
    void close() throws SQLException;
}
