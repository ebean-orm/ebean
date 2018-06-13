package io.ebeaninternal.server.persist.dml;

import io.ebeaninternal.server.core.PersistRequestBean;

import javax.persistence.OptimisticLockException;
import java.sql.SQLException;

/**
 * Delete bean handler.
 */
public class DeleteHandler extends DmlHandler {

  private final DeleteMeta meta;

  public DeleteHandler(PersistRequestBean<?> persist, DeleteMeta meta) {
    super(persist, meta.isEmptyStringAsNull());
    this.meta = meta;
  }

  /**
   * Generate and bind the delete statement.
   */
  @Override
  public void bind() throws SQLException {

    sql = meta.getSql(persistRequest);

    assert dataBind == null : "already bound";

    dataBind = getDataBind(sql, persistRequest, false);
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
