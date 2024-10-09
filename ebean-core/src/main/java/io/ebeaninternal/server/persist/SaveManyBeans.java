package io.ebeaninternal.server.persist;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.api.CoreLog;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.*;

import jakarta.persistence.PersistenceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ebeaninternal.server.persist.DmlUtil.isNullOrZero;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Saves the details for a OneToMany or ManyToMany relationship (entity beans).
 */
final class SaveManyBeans extends SaveManyBase {

  private final boolean cascade;
  private final boolean publish;
  private final BeanDescriptor<?> targetDescriptor;
  private final boolean isMap;
  private final boolean saveRecurseSkippable;
  private final DeleteMode deleteMode;
  private final boolean untouchedBeanCollection;
  private final Collection<?> collection;
  private final boolean hasOrderColumn;
  private final boolean forcedUpdate;
  private int sortOrder;
  private boolean forceOrphanRemoval;

  SaveManyBeans(DefaultPersister persister, boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
    super(persister, insertedParent, many, parentBean, request);
    this.cascade = many.cascadeInfo().isSave();
    this.publish = request.isPublish();
    this.targetDescriptor = many.targetDescriptor();
    this.isMap = many.manyType().isMap();
    this.saveRecurseSkippable = many.isSaveRecurseSkippable();
    this.deleteMode = targetDescriptor.isSoftDelete() ? DeleteMode.SOFT : DeleteMode.HARD;
    this.untouchedBeanCollection = untouchedBeanCollection();
    this.collection = cascade ? BeanCollectionUtil.getActualEntries(value) : null;
    this.hasOrderColumn = many.hasOrderColumn();
    this.forcedUpdate = request.isForcedUpdate();
  }

  /**
   * BeanCollection with no additions or removals.
   */
  private boolean untouchedBeanCollection() {
    return value instanceof BeanCollection && !((BeanCollection<?>) value).wasTouched();
  }

  @Override
  void save() {
    if (many.hasJoinTable()) {
      // check if we can save the m2m intersection in this direction
      // we only allow one direction based on first traversed basis
      boolean saveIntersectionFromThisDirection = isSaveIntersection();
      if (cascade) {
        saveAssocManyDetails();
      }
      // for ManyToMany save the 'relationship' via inserts/deletes
      // into/from the intersection table
      if (saveIntersectionFromThisDirection) {
        // only allowed on one direction of a m2m based on beanName
        saveAssocManyIntersection();
      } else {
        resetModifyState();
      }
    } else {
      if (isModifyListenMode() || hasOrderColumn) {
        // delete any removed beans / orphans
        removeAssocManyOrphans();
      }
      if (cascade) {
        // potentially deletes 'missing children' for 'stateless update'
        saveAssocManyDetails();
      }
    }
    if (!insertedParent && !untouchedBeanCollection) {
      // notify l2 cache of manyId change
      request.addUpdatedManyForL2Cache(many);
    }
  }

  private boolean isSaveIntersection() {
    if (!many.isManyToMany()) {
      // OneToMany JoinTable
      return true;
    }
    if (many.isTableManaged()) {
      List<String> tables = new ArrayList<>(3);
      tables.add(many.descriptor().baseTable());
      tables.add(many.targetDescriptor().baseTable());
      tables.add(many.intersectionTableJoin().getTable());
      // put all tables in a deterministic order
      tables.sort(Comparator.naturalOrder());

      if (transaction.isSaveAssocManyIntersection(String.join("-", tables), many.descriptor().rootName())) {
        // notify others, that we do save this transaction
        transaction.isSaveAssocManyIntersection(many.intersectionTableJoin().getTable(), many.descriptor().rootName());
        return true;
      } else {
        return false;
      }
    }
    return transaction.isSaveAssocManyIntersection(many.intersectionTableJoin().getTable(), many.descriptor().rootName());
  }

  private boolean isModifyListenMode() {
    return BeanCollection.ModifyListenMode.REMOVALS == many.modifyListenMode();
  }

  /**
   * Save the details from a OneToMany collection.
   */
  private void saveAssocManyDetails() {
    // check that the list is not null and if it is a BeanCollection
    // check that is has been populated (don't trigger lazy loading)
    if (collection != null) {
      processDetails();
    }
  }

  private void processDetails() {
    BeanProperty orderColumn = null;
    if (hasOrderColumn) {
      if (!insertedParent && canSkipForOrderColumn() && saveRecurseSkippable) {
        return;
      }
      orderColumn = targetDescriptor.orderColumn();
    }
    if (insertedParent) {
      // performance optimisation for large collections
      targetDescriptor.preAllocateIds(collection.size());
    }
    if (forcedUpdateOrphanRemoval()) {
      // deleting orphans, anything not in our detailsIds
      persister.deleteManyDetails(transaction, many.descriptor(), parentBean, many, detailIds(), deleteMode);
    }
    transaction.depth(+1);
    saveAllBeans(orderColumn);
    if (hasOrderColumn) {
      resetModifyState();
    }
    transaction.depth(-1);
  }

  private boolean forcedUpdateOrphanRemoval() {
    return !insertedParent && many.isOrphanRemoval() && (forceOrphanRemoval || forcedUpdate);
  }

  private void saveAllBeans(final BeanProperty orderColumn) {
    Object mapKeyValue = null;
    boolean skipSavingThisBean;
    boolean clearedParent = false;
    for (Object detailBean : collection) {
      sortOrder++;
      if (isMap) {
        // a map so need the key and value
        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) detailBean;
        mapKeyValue = entry.getKey();
        detailBean = entry.getValue();
      }
      if (detailBean instanceof EntityBean) {
        EntityBean detail = (EntityBean) detailBean;
        EntityBeanIntercept ebi = detail._ebean_getIntercept();
        if (many.hasJoinTable()) {
          skipSavingThisBean = targetDescriptor.isReference(ebi);
        } else {
          int originalOrder = 0;
          if (orderColumn != null) {
            originalOrder = detail._ebean_getIntercept().sortOrder();
            if (sortOrder != originalOrder) {
              detail._ebean_intercept().setSortOrder(sortOrder);
              ebi.setDirty(true);
            }
          }
          if (originalOrder == 0 && targetDescriptor.isReference(ebi)) {
            // we can skip this one
            skipSavingThisBean = true;
          } else if (ebi.isNewOrDirty()) {
            skipSavingThisBean = false;
            // set the parent bean to detailBean
            many.setParentToChild(parentBean, detail, mapKeyValue);
          } else if (many.setParentToChild(parentBean, detail, mapKeyValue, request.descriptor())) {
            skipSavingThisBean = false;
          } else {
            skipSavingThisBean = saveRecurseSkippable;
          }
        }
        if (!skipSavingThisBean) {
          persister.saveRecurse(detail, transaction, parentBean, request.flags());
          if (hasOrderColumn && !clearedParent) {
            // Clear the parent bean from the PersistenceContext (L1 cache), because the order of referenced beans might have changed
            final BeanDescriptor<?> beanDescriptor = many.descriptor();
            beanDescriptor.contextClear(transaction.persistenceContext(), beanDescriptor.getId(parentBean));
            clearedParent = true;
          }
        }
      }
    }
  }

  /**
   * Return true if we can skip based on .. no modifications to the collection and no beans are dirty.
   */
  private boolean canSkipForOrderColumn() {
    return untouchedBeanCollection && noDirtyBeans();
  }

  private boolean noDirtyBeans() {
    for (Object bean : collection) {
      if (bean instanceof EntityBean && ((EntityBean) bean)._ebean_getIntercept().isDirty()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Return the Id values of beans we know are being updated (any others are orphans)
   * If there are no IDs, null is returned.
   */
  private Set<Object> detailIds() {
    final var detailIds = new HashSet<>();
    for (Object detailBean : collection) {
      if (isMap) {
        detailBean = ((Map.Entry<?, ?>) detailBean).getValue();
      }
      if (detailBean instanceof EntityBean) {
        Object id = targetDescriptor.id(detailBean);
        if (!isNullOrZero(id)) {
          if (forcedUpdate || !((EntityBean) detailBean)._ebean_getIntercept().isNew()) {
            // Id of bean that will be updated, exclude it from orphan removal
            detailIds.add(id);
          }
        }
      }
    }
    return detailIds.isEmpty() ? null : detailIds;
  }

  /**
   * Save the additions and removals from a ManyToMany collection as inserts
   * and deletes from the intersection table.
   */
  private void saveAssocManyIntersection() {
    if (value == null) {
      return;
    }
    if (request.isQueueSaveMany()) {
      // queue/delay until bean persist request is flushed
      request.addSaveMany(this);
    } else {
      saveAssocManyIntersection(false);
    }
  }

  /**
   * Push intersection table changes onto batch flush queue.
   */
  @Override
  public void saveBatch() {
    saveAssocManyIntersection(true);
  }

  private void saveAssocManyIntersection(boolean queue) {
    final boolean vanillaCollection = !(value instanceof BeanCollection<?>);
    if (vanillaCollection || forcedUpdate) {
      // delete all intersection rows and then treat all
      // beans in the collection as additions
      persister.deleteManyIntersection(parentBean, many, transaction, publish, queue);
    }

    Collection<?> deletions = null;
    Collection<?> additions;
    if (insertedParent || vanillaCollection || forcedUpdate) {
      // treat everything in the list/set/map as an intersection addition
      if (value instanceof Map<?, ?>) {
        additions = ((Map<?, ?>) value).values();
      } else if (value instanceof Collection<?>) {
        additions = (Collection<?>) value;
      } else {
        throw new PersistenceException("Unhandled ManyToMany type " + value.getClass().getName() + " for " + many.fullName());
      }
      if (!vanillaCollection) {
        BeanCollection<?> manyValue = (BeanCollection<?>) value;
        setListenMode(manyValue, many);
        manyValue.modifyReset();
      }
    } else {
      // BeanCollection so get the additions/deletions
      BeanCollection<?> manyValue = (BeanCollection<?>) value;
      if (setListenMode(manyValue, many)) {
        additions = manyValue.actualDetails();
      } else {
        additions = manyValue.modifyAdditions();
        deletions = manyValue.modifyRemovals();
      }
      // reset so the changes are only processed once
      manyValue.modifyReset();
    }

    transaction.depth(+1);
    if (deletions != null && !deletions.isEmpty()) {
      for (Object other : deletions) {
        EntityBean otherDelete = (EntityBean) other;
        // the object from the 'other' side of the ManyToMany
        // build a intersection row for 'delete'
        IntersectionRow intRow = many.buildManyToManyMapBean(parentBean, otherDelete, publish);
        SpiSqlUpdate sqlDelete = intRow.createDelete(server, DeleteMode.HARD);
        persister.executeOrQueue(sqlDelete, transaction, queue, BatchControl.DELETE_QUEUE);
      }
    }
    if (additions != null && !additions.isEmpty()) {
      for (Object other : additions) {
        EntityBean otherBean = (EntityBean) other;
        // the object from the 'other' side of the ManyToMany
        if (deletions != null && deletions.remove(otherBean)) {
          String msg = "Inserting and Deleting same object? " + otherBean;
          if (transaction.isLogSummary()) {
            transaction.logSummary(msg);
          }
          CoreLog.log.log(WARNING, msg);
        } else {
          if (!many.hasImportedId(otherBean)) {
            throw new PersistenceException("ManyToMany bean does not have an Id value? " + otherBean);
          } else {
            // build a intersection row for 'insert'
            IntersectionRow intRow = many.buildManyToManyMapBean(parentBean, otherBean, publish);
            SpiSqlUpdate sqlInsert = intRow.createInsert(server);
            persister.executeOrQueue(sqlInsert, transaction, queue, BatchControl.INSERT_QUEUE);
          }
        }
      }
    }
    // decrease the depth back to what it was
    transaction.depth(-1);
  }

  private boolean isChangedProperty() {
    return request.isChangedProperty(many.propertyIndex());
  }

  private void removeAssocManyOrphans() {
    if (value == null) {
      return;
    }
    if (!(value instanceof BeanCollection<?>)) {
      forceOrphanRemoval = !insertedParent && isChangedProperty();
    } else {
      BeanCollection<?> c = (BeanCollection<?>) value;
      Set<?> modifyRemovals = c.modifyRemovals();
      if (insertedParent) {
        // after insert set the modify listening mode for private owned etc
        c.setModifyListening(many.modifyListenMode());
      }
      // We must not reset when we still have to update other entities in the collection and set their new orderColumn value
      if (!hasOrderColumn) {
        c.modifyReset();
      }
      if (modifyRemovals == null || modifyRemovals.isEmpty()) {
        forceOrphanRemoval = !insertedParent && isChangedProperty();
      } else {
        for (Object removedBean : modifyRemovals) {
          if (removedBean instanceof EntityBean) {
            EntityBean eb = (EntityBean) removedBean;
            if (eb._ebean_intercept().isOrphanDelete()) {
              // only delete if the bean was loaded meaning that it is known to exist in the DB
              persister.deleteRequest(persister.createDeleteRemoved(removedBean, transaction, request.flags()));
            }
          }
        }
      }
    }
  }

  /**
   * Check if we need to set the listen mode (on new collections persisted for the first time).
   */
  private boolean setListenMode(BeanCollection<?> manyValue, BeanPropertyAssocMany<?> prop) {
    BeanCollection.ModifyListenMode mode = manyValue.modifyListening();
    if (mode == null) {
      // new collection persisted for the first time
      manyValue.setModifyListening(prop.modifyListenMode());
      return true;
    }
    return false;
  }
}
