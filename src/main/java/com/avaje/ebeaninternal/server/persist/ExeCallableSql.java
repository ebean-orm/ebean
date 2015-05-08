package com.avaje.ebeaninternal.server.persist;

import java.sql.CallableStatement;
import java.sql.SQLException;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.SpiCallableSql;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequestCallableSql;
import com.avaje.ebeaninternal.server.core.PstmtBatch;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.util.BindParamsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the execution of CallableSql requests.
 */
public class ExeCallableSql {

  private static final Logger logger = LoggerFactory.getLogger(ExeCallableSql.class);

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
      if (!batchThisRequest && cstmt != null) {
        try {
          cstmt.close();
        } catch (SQLException e) {
          logger.error(null, e);
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
    if (batchThisRequest) {
      cstmt = pstmtFactory.getCstmt(t, logSql, sql, request);
    } else {
      if (logSql) {
        t.logSql(sql);
      }
      cstmt = pstmtFactory.getCstmt(t, sql);
    }

    if (callableSql.getTimeout() > 0) {
      cstmt.setQueryTimeout(callableSql.getTimeout());
    }

    String bindLog = null;
    if (!bindParams.isEmpty()) {
      bindLog = binder.bind(bindParams, new DataBind(cstmt));
    }

    request.setBindLog(bindLog);

    // required to read OUT params later
    request.setBound(bindParams, cstmt);
    return cstmt;
  }
}
