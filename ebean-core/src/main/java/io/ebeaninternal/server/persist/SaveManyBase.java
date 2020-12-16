package io.ebeaninternal.server.persist;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Base for saving entity bean collections and element collections.
 */
abstract class SaveManyBase implements SaveMany {

  private static final Logger log = LoggerFactory.getLogger(SaveManyBase.class);

  final DefaultPersister persister;
  final PersistRequestBean<?> request;
  final SpiEbeanServer server;
  final boolean insertedParent;
  final BeanPropertyAssocMany<?> many;
  final SpiTransaction transaction;
  final EntityBean parentBean;
  final Object value;

  SaveManyBase(DefaultPersister persister, boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
    this.persister = persister;
    this.request = request;
    this.server = request.getServer();
    this.many = many;
    this.parentBean = parentBean;
    this.transaction = request.getTransaction();
    this.value = many.getValue(parentBean);
    this.insertedParent = insertedParent;
    if (!insertedParent) {
      request.setUpdatedMany();
    }
  }

  /**
   * Save the collection.
   */
  abstract void save();

  void preElementCollectionUpdate() {
    if (!insertedParent) {
      request.preElementCollectionUpdate();
      persister.addToFlushQueue(many.deleteByParentId(request.getBeanId(), null), transaction, 1);
    }
  }

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

  void postElementCollectionUpdate() {
    if (!insertedParent) {
      if (request.isNotifyCache()) {
        try {
          String asJson = many.jsonWriteCollection(value);
          request.addCollectionChange(many.getName(), asJson);
        } catch (IOException e) {
          log.error("Error build element collection entry for L2 cache", e);
        }
      }
    }
  }
}
