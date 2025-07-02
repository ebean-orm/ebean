package io.ebeaninternal.server.persist.dml;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.util.StringHelper;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.persist.BeanPersister;

import java.sql.SQLException;

/**
 * Bean persister that uses the Handler and Meta objects.
 * <p>
 * The design of this is based on the immutable Meta objects. They hold a
 * information in the form of lists of Bindable objects. This effectively
 * flattens the structure of the bean with embedded and associated objects into
 * a flat list of Bindable objects.
 * </p>
 */
final class DmlBeanPersister implements BeanPersister {

  private final DatabasePlatform dbPlatform;
  private final UpdateMeta updateMeta;
  private final InsertMeta insertMeta;
  private final DeleteMeta deleteMeta;

  DmlBeanPersister(DatabasePlatform dbPlatform, UpdateMeta updateMeta, InsertMeta insertMeta, DeleteMeta deleteMeta) {
    this.dbPlatform = dbPlatform;
    this.updateMeta = updateMeta;
    this.insertMeta = insertMeta;
    this.deleteMeta = deleteMeta;
  }

  /**
   * execute the bean delete request.
   */
  @Override
  public int delete(PersistRequestBean<?> request) {
    return execute(request, new DeleteHandler(request, deleteMeta));
  }

  /**
   * execute the bean insert request.
   */
  @Override
  public void insert(PersistRequestBean<?> request) {
    execute(request, new InsertHandler(request, insertMeta));
  }

  /**
   * execute the bean update request.
   */
  @Override
  public void update(PersistRequestBean<?> request) {
    execute(request, new UpdateHandler(request, updateMeta));
  }

  /**
   * execute request taking batching into account.
   */
  private int execute(PersistRequestBean<?> request, PersistHandler handler) {
    boolean batched = request.isBatched();
    try {
      handler.bind();
      if (batched) {
        handler.addBatch();
        return -1;

      } else {
        return handler.executeNoBatch();
      }
    } catch (SQLException e) {
      // log the error to the transaction log
      String msg = "Error: " + StringHelper.removeNewLines(e.getMessage());
      if (request.transaction().isLogSummary()) {
        request.transaction().logSummary(msg);
      }
      request.onFailedUpdateUndoGeneratedProperties();
      throw dbPlatform.translate(msg, e);
    } finally {
      if (!batched) {
        handler.close();
      }
    }
  }

}
