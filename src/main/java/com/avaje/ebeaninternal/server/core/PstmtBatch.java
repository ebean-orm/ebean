package com.avaje.ebeaninternal.server.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * If Oracle supported the JDBC api fully this would not be required.
 */
public interface PstmtBatch {

	void setBatchSize(PreparedStatement pstmt, int batchSize);

	void addBatch(PreparedStatement pstmt) throws SQLException;

  int executeBatch(PreparedStatement pstmt, int expectedRows, String sql, boolean occCheck) throws SQLException;

}
