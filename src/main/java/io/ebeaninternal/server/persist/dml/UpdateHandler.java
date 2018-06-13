package io.ebeaninternal.server.persist.dml;

import io.ebeaninternal.api.SpiUpdatePlan;
import io.ebeaninternal.server.core.PersistRequestBean;

import javax.persistence.OptimisticLockException;
import java.sql.SQLException;

/**
 * Update bean handler.
 */
public class UpdateHandler extends DmlHandler {

  private final UpdateMeta meta;

  private boolean emptySetClause;

  public UpdateHandler(PersistRequestBean<?> persist, UpdateMeta meta) {
    super(persist, meta.isEmptyStringAsNull());
    this.meta = meta;
  }

  /**
   * Generate and bind the update statement.
   */
  @Override
  public void bind() throws SQLException {

    SpiUpdatePlan updatePlan = meta.getUpdatePlan(persistRequest);

    if (updatePlan.isEmptySetClause()) {
      emptySetClause = true;
      return;
    }

    sql = updatePlan.getSql();

    assert dataBind == null : "already bound";

    dataBind = getDataBind(sql, persistRequest, false);
    meta.bind(persistRequest, this, updatePlan);

    setUpdateGenValues();

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
