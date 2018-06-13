package io.ebeaninternal.server.persist;

import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.SpiUpdate;
import io.ebeaninternal.server.core.PersistRequestOrmUpdate;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.type.DataBind;
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
  ExeOrmUpdate(Binder binder, PstmtFactory pstmtFactory) {
    this.pstmtFactory = pstmtFactory;
    this.binder = binder;
  }

  /**
   * Execute the orm update request.
   */
  public int execute(PersistRequestOrmUpdate request) {

    boolean batchThisRequest = request.isBatchThisRequest();

    try (DataBind dataBind = bindStmt(request, batchThisRequest)) {
      if (batchThisRequest) {
        dataBind.getPstmt().addBatch();
        // return -1 to indicate batch mode
        return -1;
      } else {
        try (PreparedStatement pstmt = dataBind.getPstmt()) {
          SpiUpdate<?> ormUpdate = request.getOrmUpdate();
          if (ormUpdate.getTimeout() > 0) {
            pstmt.setQueryTimeout(ormUpdate.getTimeout());
          }
          int rowCount = pstmt.executeUpdate();
          request.checkRowCount(rowCount);
          request.postExecute();
          return rowCount;
        }
      }

    } catch (SQLException ex) {
      throw new PersistenceException("Error executing: " + request.getOrmUpdate().getGeneratedSql(), ex);
    }
  }

  /**
   * Convert bean and property names to db table and columns.
   */
  private String translate(PersistRequestOrmUpdate request, String sql) {

    BeanDescriptor<?> descriptor = request.getBeanDescriptor();
    return descriptor.convertOrmUpdateToSql(sql);
  }

  private DataBind bindStmt(PersistRequestOrmUpdate request, boolean batchThisRequest) throws SQLException {

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

    DataBind dataBind;
    if (batchThisRequest) {
      dataBind = pstmtFactory.getBatchedPDataBind(t, logSql, sql, request);
    } else {
      dataBind = pstmtFactory.getPDataBind(t, logSql, sql, false);
    }
    String bindLog = null;
    if (!bindParams.isEmpty()) {
      bindLog = binder.bind(bindParams, dataBind);
    }

    request.setBindLog(bindLog);
    return dataBind;
  }
}
