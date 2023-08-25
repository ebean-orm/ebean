package io.ebeaninternal.server.persist.dml;

import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.SpiUpdatePlan;
import io.ebeaninternal.server.core.PersistRequestBean;

import javax.persistence.OptimisticLockException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Update bean handler.
 */
public final class UpdateHandler extends DmlHandler {

  private final UpdateMeta meta;
  private boolean emptySetClause;

  UpdateHandler(PersistRequestBean<?> persist, UpdateMeta meta) {
    super(persist);
    this.meta = meta;
  }

  @Override
  public boolean isUpdate() {
    return true;
  }

  /**
   * Generate and bind the update statement.
   */
  @Override
  public void bind() throws SQLException {
    SpiUpdatePlan updatePlan = meta.updatePlan(persistRequest);
    if (updatePlan.isEmptySetClause()) {
      emptySetClause = true;
      return;
    }

    sql = updatePlan.sql();
    SpiTransaction t = persistRequest.transaction();
    PreparedStatement pstmt;
    if (persistRequest.isBatched()) {
      pstmt = pstmtBatch(t, sql, persistRequest, false);
    } else {
      pstmt = pstmt(t, sql, false);
    }
    dataBind = bind(pstmt);
    meta.bind(persistRequest, this, updatePlan);
    if (persistRequest.isBatched()) {
      batchedPstmt.registerInputStreams(dataBind.getInputStreams());
    }
    logSql(sql);
  }

  @Override
  public void addBatch() throws SQLException {
    if (!emptySetClause) {
      super.addBatch();
    }
  }

  /**
   * Execute the update in non-batch.
   */
  @Override
  public int execute() throws SQLException, OptimisticLockException {
    if (!emptySetClause) {
      int rowCount = dataBind.executeUpdate();
      checkRowCount(rowCount);
      return rowCount;
    }
    return 0;
  }

}
