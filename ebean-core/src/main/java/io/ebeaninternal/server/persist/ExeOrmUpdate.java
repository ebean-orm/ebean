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
final class ExeOrmUpdate {

  private final Binder binder;
  private final PstmtFactory pstmtFactory;

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
        SpiUpdate<?> ormUpdate = request.ormUpdate();
        if (ormUpdate.timeout() > 0) {
          pstmt.setQueryTimeout(ormUpdate.timeout());
        }
        int rowCount = pstmt.executeUpdate();
        request.checkRowCount(rowCount);
        request.postExecute();
        return rowCount;
      }

    } catch (SQLException ex) {
      throw new PersistenceException("Error executing: " + request.ormUpdate().getGeneratedSql(), ex);

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
    BeanDescriptor<?> descriptor = request.descriptor();
    return descriptor.convertOrmUpdateToSql(sql);
  }

  private PreparedStatement bindStmt(PersistRequestOrmUpdate request, boolean batchThisRequest) throws SQLException {
    request.startBind(batchThisRequest);
    SpiUpdate<?> ormUpdate = request.ormUpdate();
    SpiTransaction t = request.transaction();

    String sql = ormUpdate.updateStatement();
    // convert bean and property names to table and
    // column names if required
    sql = translate(request, sql);
    BindParams bindParams = ormUpdate.bindParams();
    // process named parameters if required
    sql = BindParamsParser.parse(bindParams, sql);
    ormUpdate.setGeneratedSql(sql);

    PreparedStatement pstmt;
    if (batchThisRequest) {
      pstmt = pstmtFactory.pstmtBatch(t, sql, request);
    } else {
      if (t.isLogSql()) {
        t.logSql(sql);
      }
      pstmt = pstmtFactory.pstmt(t, sql, false);
    }
    String bindLog = null;
    if (!bindParams.isEmpty()) {
      bindLog = binder.bind(bindParams, pstmt, t.internalConnection());
    }
    request.setBindLog(bindLog);
    return pstmt;
  }
}
