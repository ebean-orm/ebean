package io.ebeaninternal.server.persist;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;

/**
 * Base for saving entity bean collections and element collections.
 */
abstract class SaveManyBase {

  final PersistRequestBean<?> request;
  final SpiEbeanServer server;
  final boolean insertedParent;
  final BeanPropertyAssocMany<?> many;
  final SpiTransaction transaction;
  final EntityBean parentBean;
  final Object value;

  SaveManyBase(boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
    this.request = request;
    this.server = request.getServer();
    this.insertedParent = insertedParent;
    this.many = many;
    this.parentBean = parentBean;
    this.transaction = request.getTransaction();
    this.value = many.getValue(parentBean);
  }

  /**
   * Save the collection.
   */
  abstract void save();

  void resetModifyState() {
    if (value instanceof BeanCollection<?>) {
      modifyListenReset((BeanCollection<?>) value);
    }
  }

  void modifyListenReset(BeanCollection<?> c) {
    if (insertedParent) {
      // after insert set the modify listening mode for private owned etc
      c.setModifyListening(many.getModifyListenMode());
    }
    c.modifyReset();
  }
}
