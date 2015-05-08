package com.avaje.ebeaninternal.server.persist;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PstmtBatch;

/**
 * Factory for creating Statements.
 * <p>
 * This is only used by CallableSql and UpdateSql requests and does not support
 * getGeneratedKeys.
 * </p>
 */
public class PstmtFactory {


	private final PstmtBatch pstmtBatch;
	
	public PstmtFactory(PstmtBatch pstmtBatch) {
		this.pstmtBatch = pstmtBatch;
	}
	
	/**
	 * Get a callable statement without any batching.
	 */
	public CallableStatement getCstmt(SpiTransaction t, String sql) throws SQLException {
		Connection conn = t.getInternalConnection();
		return conn.prepareCall(sql);
	}

	/**
	 * Get a prepared statement without any batching.
	 */
	public PreparedStatement getPstmt(SpiTransaction t, String sql) throws SQLException {
		Connection conn = t.getInternalConnection();
		return conn.prepareStatement(sql);
	}

	/**
	 * Return a prepared statement taking into account batch requirements.
	 */
	public PreparedStatement getPstmt(SpiTransaction t, boolean logSql, String sql, BatchPostExecute batchExe)
			throws SQLException {

		BatchedPstmtHolder batch = t.getBatchControl().getPstmtHolder();
		PreparedStatement stmt = batch.getStmt(sql, batchExe);

		if (stmt != null) {
			return stmt;
		}

		if (logSql){
		    t.logSql(sql);
		}
		
		Connection conn = t.getInternalConnection();
		stmt = conn.prepareStatement(sql);

		if (pstmtBatch != null){
        	pstmtBatch.setBatchSize(stmt, t.getBatchControl().getBatchSize());
        }
		
		BatchedPstmt bs = new BatchedPstmt(stmt, false, sql, pstmtBatch, false);
		batch.addStmt(bs, batchExe);
		return stmt;
	}

	/**
	 * Return a callable statement taking into account batch requirements.
	 */
	public CallableStatement getCstmt(SpiTransaction t, boolean logSql, String sql, BatchPostExecute batchExe)
			throws SQLException {

		BatchedPstmtHolder batch = t.getBatchControl().getPstmtHolder();
		CallableStatement stmt = (CallableStatement) batch.getStmt(sql, batchExe);

		if (stmt != null) {
			return stmt;
		}
		
		if (logSql){
		    t.logSql(sql);
		}

		Connection conn = t.getInternalConnection();
		stmt = conn.prepareCall(sql);

		BatchedPstmt bs = new BatchedPstmt(stmt, false, sql, pstmtBatch, false);
		batch.addStmt(bs, batchExe);
		return stmt;
	}
}
