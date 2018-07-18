package io.ebeaninternal.server.persist.dml;

import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.PersistRequestBean;

import javax.persistence.OptimisticLockException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Delete bean handler.
 */
public class DeleteHandler extends DmlHandler {

  private final DeleteMeta meta;

  DeleteHandler(PersistRequestBean<?> persist, DeleteMeta meta) {
    super(persist, meta.isEmptyStringAsNull());
    this.meta = meta;
  }

  @Override
  public boolean isUpdate() {
    return false;
  }

  /**
   * Generate and bind the delete statement.
   */
  @Override
  public void bind() throws SQLException {

    sql = meta.getSql(persistRequest);
    SpiTransaction t = persistRequest.getTransaction();

    PreparedStatement pstmt;
    if (persistRequest.isBatched()) {
      pstmt = getPstmt(t, sql, persistRequest, false);
    } else {
      pstmt = getPstmt(t, sql, false);
    }
    dataBind = bind(pstmt);
    meta.bind(persistRequest, this);
    logSql(sql);
  }

  /**
   * Execute the delete non-batch.
   */
  @Override
  public int execute() throws SQLException, OptimisticLockException {
    int rowCount = dataBind.executeUpdate();
    checkRowCount(rowCount);
    return rowCount;
  }

}
