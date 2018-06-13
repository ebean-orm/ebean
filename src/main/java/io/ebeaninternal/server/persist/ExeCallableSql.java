package io.ebeaninternal.server.persist;

import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiCallableSql;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.PersistRequestCallableSql;
import io.ebeaninternal.server.type.DataBind;
import io.ebeaninternal.server.util.BindParamsParser;

import javax.persistence.PersistenceException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Handles the execution of CallableSql requests.
 */
class ExeCallableSql {

  private final Binder binder;

  private final PstmtFactory pstmtFactory;

  ExeCallableSql(Binder binder, PstmtFactory pstmtFactory) {
    this.binder = binder;
    this.pstmtFactory = pstmtFactory;
  }

  /**
   * execute the CallableSql requests.
   */
  public int execute(PersistRequestCallableSql request) {

    boolean batchThisRequest = request.isBatchThisRequest();

    try (DataBind dataBind = bindStmt(request, batchThisRequest)) {
      if (batchThisRequest) {
        dataBind.getPstmt().addBatch();
        // return -1 to indicate batch mode
        return -1;
      } else {
        try (PreparedStatement pstmt = dataBind.getPstmt()) {
          // handles executeOverride() and also
          // reading of registered OUT parameters
          int rowCount = request.executeUpdate();
          request.postExecute();
          return rowCount;
        }
      }

    } catch (SQLException ex) {
      throw new PersistenceException(ex);
    }
  }


  private DataBind bindStmt(PersistRequestCallableSql request, boolean batchThisRequest) throws SQLException {

    request.startBind(batchThisRequest);
    SpiCallableSql callableSql = request.getCallableSql();
    SpiTransaction t = request.getTransaction();

    String sql = callableSql.getSql();

    BindParams bindParams = callableSql.getBindParams();

    // process named parameters if required
    sql = BindParamsParser.parse(bindParams, sql);

    boolean logSql = request.isLogSql();

    DataBind dataBind;
    if (batchThisRequest) {
      dataBind = pstmtFactory.getBatchedCDataBind(t, logSql, sql, request);
    } else {
      dataBind = pstmtFactory.getCDataBind(t, logSql, sql);
    }

    if (callableSql.getTimeout() > 0) {
      dataBind.getPstmt().setQueryTimeout(callableSql.getTimeout());
    }

    String bindLog = null;
    if (!bindParams.isEmpty()) {
      bindLog = binder.bind(bindParams, dataBind);
    }

    request.setBindLog(bindLog);

    // required to read OUT params later
    request.setBound(bindParams, (CallableStatement) dataBind.getPstmt());
    return dataBind;
  }
}
