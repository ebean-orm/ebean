package com.avaje.ebeaninternal.server.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * If Oracle supported the JDBC api fully this would not be required.
 */
public interface PstmtBatch {

	public void setBatchSize(PreparedStatement pstmt, int batchSize);

	public void addBatch(PreparedStatement pstmt) throws SQLException;

    public int executeBatch(PreparedStatement pstmt, int expectedRows, String sql, boolean occCheck) throws SQLException;

}
