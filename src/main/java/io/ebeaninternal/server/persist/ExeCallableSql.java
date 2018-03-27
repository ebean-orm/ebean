package io.ebeaninternal.server.persist;

import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiCallableSql;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.PersistRequestCallableSql;
import io.ebeaninternal.server.util.BindParamsParser;

import javax.persistence.PersistenceException;
import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * Handles the execution of CallableSql requests.
 */
class ExeCallableSql {

  private final Binder binder;

  private final PstmtFactory pstmtFactory;

  ExeCallableSql(Binder binder) {
    this.binder = binder;
    this.pstmtFactory = new PstmtFactory();
  }

  /**
   * execute the CallableSql requests.
   */
  public int execute(PersistRequestCallableSql request) {

    boolean batchThisRequest = request.isBatchThisRequest();

    CallableStatement cstmt = null;
    try {
      cstmt = bindStmt(request, batchThisRequest);
      if (batchThisRequest) {
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
      if (!batchThisRequest) {
        JdbcClose.close(cstmt);
      }
    }
  }


  private CallableStatement bindStmt(PersistRequestCallableSql request, boolean batchThisRequest) throws SQLException {

    request.startBind(batchThisRequest);
    SpiCallableSql callableSql = request.getCallableSql();
    SpiTransaction t = request.getTransaction();

    String sql = callableSql.getSql();

    BindParams bindParams = callableSql.getBindParams();

    // process named parameters if required
    sql = BindParamsParser.parse(bindParams, sql);

    boolean logSql = request.isLogSql();

    CallableStatement cstmt;
    if (batchThisRequest) {
      cstmt = pstmtFactory.getCstmt(t, logSql, sql, request);
    } else {
      if (logSql) {
        t.logSql(TrimLogSql.trim(sql));
      }
      cstmt = pstmtFactory.getCstmt(t, sql);
    }

    if (callableSql.getTimeout() > 0) {
      cstmt.setQueryTimeout(callableSql.getTimeout());
    }

    String bindLog = null;
    if (!bindParams.isEmpty()) {
      bindLog = binder.bind(bindParams, cstmt, t.getInternalConnection());
    }

    request.setBindLog(bindLog);

    // required to read OUT params later
    request.setBound(bindParams, cstmt);
    return cstmt;
  }
}
