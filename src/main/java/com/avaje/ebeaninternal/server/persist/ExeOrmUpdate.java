package com.avaje.ebeaninternal.server.persist;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.api.SpiUpdate;
import com.avaje.ebeaninternal.server.core.PersistRequestOrmUpdate;
import com.avaje.ebeaninternal.server.core.PstmtBatch;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.util.BindParamsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes the UpdateSql requests.
 */
public class ExeOrmUpdate {

  private static final Logger logger = LoggerFactory.getLogger(ExeOrmUpdate.class);

  private final Binder binder;

  private final PstmtFactory pstmtFactory;

  /**
   * Create with a given binder.
   */
  public ExeOrmUpdate(Binder binder, PstmtBatch pstmtBatch) {
    this.pstmtFactory = new PstmtFactory(pstmtBatch);
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
        PstmtBatch pstmtBatch = request.getPstmtBatch();
        if (pstmtBatch != null) {
          pstmtBatch.addBatch(pstmt);
        } else {
          pstmt.addBatch();
        }
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
      if (!batchThisRequest && pstmt != null) {
        try {
          pstmt.close();
        } catch (SQLException e) {
          logger.error(null, e);
        }
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
      pstmt = pstmtFactory.getPstmt(t, sql);
    }

    String bindLog = null;
    if (!bindParams.isEmpty()) {
      bindLog = binder.bind(bindParams, new DataBind(pstmt));
    }

    request.setBindLog(bindLog);
    return pstmt;
  }
}
