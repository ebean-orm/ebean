package com.avaje.ebeaninternal.server.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import com.avaje.ebean.config.PstmtDelegate;
import com.avaje.ebeaninternal.api.ClassUtil;
import com.avaje.ebeaninternal.server.core.PstmtBatch;


/**
 * Oracle specific handling of JDBC batching.
 * <p>
 * I guess some people don't need to follow the jdbc specification.
 * </p>
 * 
 * @author rbygrave
 * @author imario
 */
public class OraclePstmtBatch implements PstmtBatch {

	private final PstmtDelegate pstmtDelegate;

    /**
     * OraclePreparedStatement.setExecuteBatch() method.
     */
	private static final Method METHOD_SET_EXECUTE_BATCH;
	
	/**
	 * OraclePreparedStatement.sendBatch() method.
	 */
	private static final Method METHOD_SEND_BATCH;

	private static final RuntimeException INIT_EXCEPTION;

	static {
		RuntimeException initException = null;
		Method mSetExecuteBatch = null;
		Method mSendBatch = null;

		try {
			Class<?> ops = ClassUtil.forName("oracle.jdbc.OraclePreparedStatement");

			mSetExecuteBatch = ops.getMethod("setExecuteBatch", new Class[] { int.class });
			mSendBatch = ops.getMethod("sendBatch");
		
		} catch (NoSuchMethodException e) {
			initException = new RuntimeException("problems initializing oracle reflection", e);
			initException.fillInStackTrace();
		
		} catch (ClassNotFoundException e) {
			initException = new RuntimeException("problems initializing oracle reflection", e);
			initException.fillInStackTrace();
		}

		INIT_EXCEPTION = initException;
		METHOD_SET_EXECUTE_BATCH = mSetExecuteBatch;
		METHOD_SEND_BATCH = mSendBatch;
	}

	public OraclePstmtBatch(PstmtDelegate pstmtDelegate) {
		this.pstmtDelegate = pstmtDelegate;
	}

	public void setBatchSize(PreparedStatement pstmt, int batchSize) {
		if (INIT_EXCEPTION != null) {
			throw INIT_EXCEPTION;
		}

		try {
            // invoke setExecuteBatch(batchSize+1);
			METHOD_SET_EXECUTE_BATCH.invoke(pstmtDelegate.unwrap(pstmt), batchSize+1);
		} catch (IllegalAccessException e) {
			String m = "Error with Oracle setExecuteBatch "+(batchSize+1);
			throw new RuntimeException(m, e);
		} catch (InvocationTargetException e) {
			String m = "Error with Oracle setExecuteBatch "+(batchSize+1);
			throw new RuntimeException(m, e);
		}
	}
	
	/**
	 * Simply calls standard pstmt.addBatch().
	 */
	public void addBatch(PreparedStatement pstmt) throws SQLException {
		pstmt.executeUpdate();
	}
	
	public int executeBatch(PreparedStatement pstmt, int expectedRows, String sql, boolean occCheck) throws SQLException {
		
		if (INIT_EXCEPTION != null) {
			throw INIT_EXCEPTION;
		}

		int rows;
		try {
		    // invoke sendBatch();
			rows = ((Integer) METHOD_SEND_BATCH.invoke(pstmtDelegate.unwrap(pstmt))).intValue();
		
		} catch (IllegalAccessException e) {
            String msg = "Error invoking Oracle sendBatch method via reflection";
			throw new PersistenceException(msg,e);
		
		} catch (InvocationTargetException e) {
		    String msg = "Error invoking Oracle sendBatch method via reflection";
			throw new PersistenceException(msg, e);
		}
		if (occCheck && rows != expectedRows) {
		    String msg = "Batch execution expected "+expectedRows+" but got "+rows+"  sql:"+sql;
			throw new OptimisticLockException(msg);
		}
		
		return rows;
	}

}
