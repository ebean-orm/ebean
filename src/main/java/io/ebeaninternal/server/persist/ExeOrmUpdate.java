package io.ebeaninternal.server.persist;

import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.SpiUpdate;
import io.ebeaninternal.server.core.PersistRequestOrmUpdate;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.util.BindParamsParser;

import javax.persistence.PersistenceException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Executes the UpdateSql requests.
 */
class ExeOrmUpdate {

  private final Binder binder;

  private final PstmtFactory pstmtFactory;

  /**
   * Create with a given binder.
   */
  ExeOrmUpdate(Binder binder) {
    this.pstmtFactory = new PstmtFactory();
    this.binder = binder;
  }

  /**
   * Execute the orm update request.
   */
  public int execute(PersistRequestOrmUpdate request) {

    boolean batchThisRequest = request.isBatchThisRequest();

    PreparedStatement pstmt = null;
    try {
      pstmt = bindStmt(request, batchThisRequest);
      if (batchThisRequest) {
        pstmt.addBatch();
        // return -1 to indicate batch mode
        return -1;
      } else {
        SpiUpdate<?> ormUpdate = request.getOrmUpdate();
        if (ormUpdate.getTimeout() > 0) {
          pstmt.setQueryTimeout(ormUpdate.getTimeout());
        }
        int rowCount = pstmt.executeUpdate();
        request.checkRowCount(rowCount);
        request.postExecute();
        return rowCount;
      }

    } catch (SQLException ex) {
      throw new PersistenceException("Error executing: " + request.getOrmUpdate().getGeneratedSql(), ex);

    } finally {
      if (!batchThisRequest) {
        JdbcClose.close(pstmt);
      }
    }
  }

  /**
   * Convert bean and property names to db table and columns.
   */
  private String translate(PersistRequestOrmUpdate request, String sql) {

    BeanDescriptor<?> descriptor = request.getBeanDescriptor();
    return descriptor.convertOrmUpdateToSql(sql);
  }

  private PreparedStatement bindStmt(PersistRequestOrmUpdate request, boolean batchThisRequest) throws SQLException {

    request.startBind(batchThisRequest);
    SpiUpdate<?> ormUpdate = request.getOrmUpdate();
    SpiTransaction t = request.getTransaction();

    String sql = ormUpdate.getUpdateStatement();

    // convert bean and property names to table and
    // column names if required
    sql = translate(request, sql);

    BindParams bindParams = ormUpdate.getBindParams();

    // process named parameters if required
    sql = BindParamsParser.parse(bindParams, sql);

    ormUpdate.setGeneratedSql(sql);

    boolean logSql = request.isLogSql();

    PreparedStatement pstmt;
    if (batchThisRequest) {
      pstmt = pstmtFactory.getPstmt(t, logSql, sql, request);
    } else {
      if (logSql) {
        t.logSql(sql);
      }
      pstmt = pstmtFactory.getPstmt(t, sql, false);
    }

    String bindLog = null;
    if (!bindParams.isEmpty()) {
      bindLog = binder.bind(bindParams, pstmt, t.getInternalConnection());
    }

    request.setBindLog(bindLog);
    return pstmt;
  }
}
