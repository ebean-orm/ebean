package io.ebeaninternal.server.persist.dml;

import io.ebean.util.StringHelper;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.persist.BeanPersister;

import javax.persistence.PersistenceException;
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
public final class DmlBeanPersister implements BeanPersister {

  private final UpdateMeta updateMeta;

  private final InsertMeta insertMeta;

  private final DeleteMeta deleteMeta;


  public DmlBeanPersister(UpdateMeta updateMeta, InsertMeta insertMeta, DeleteMeta deleteMeta) {

    this.updateMeta = updateMeta;
    this.insertMeta = insertMeta;
    this.deleteMeta = deleteMeta;
  }

  /**
   * execute the bean delete request.
   */
  @Override
  public int delete(PersistRequestBean<?> request) {

    DeleteHandler delete = new DeleteHandler(request, deleteMeta);
    return execute(request, delete);
  }

  /**
   * execute the bean insert request.
   */
  @Override
  public void insert(PersistRequestBean<?> request) {

    InsertHandler insert = new InsertHandler(request, insertMeta);
    execute(request, insert);
  }

  /**
   * execute the bean update request.
   */
  @Override
  public void update(PersistRequestBean<?> request) {

    UpdateHandler update = new UpdateHandler(request, updateMeta);
    execute(request, update);
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
        return handler.execute();
      }

    } catch (SQLException e) {
      // log the error to the transaction log
      String errMsg = StringHelper.replaceStringMulti(e.getMessage(), new String[]{"\r", "\n"}, "\\n ");
      String msg = "ERROR executing DML bindLog[" + handler.getBindLog() + "] error[" + errMsg + "]";
      if (request.getTransaction().isLogSummary()) {
        request.getTransaction().logSummary(msg);
      }
      throw new PersistenceException(msg, e);

    } finally {
      if (!batched && handler != null) {
        handler.close();
      }
    }
  }

}
