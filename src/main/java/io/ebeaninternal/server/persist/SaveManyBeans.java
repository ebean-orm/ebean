package io.ebeaninternal.server.persist;

import io.ebean.SqlUpdate;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanCollectionUtil;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.IntersectionRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Saves the details for a OneToMany or ManyToMany relationship (entity beans).
 */
class SaveManyBeans extends SaveManyBase {

  private static final Logger log = LoggerFactory.getLogger(SaveManyBeans.class);

  private final boolean cascade;
  private final boolean publish;
  private final BeanDescriptor<?> targetDescriptor;
  private final boolean isMap;
  private final boolean saveRecurseSkippable;
  private final DeleteMode deleteMode;

  private Collection<?> collection;
  private DefaultPersister persister;
  private boolean deleteMissing;
  private int sortOrder;

  SaveManyBeans(boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request, DefaultPersister persister) {
    super(insertedParent, many, parentBean, request);
    this.persister = persister;
    this.cascade = many.getCascadeInfo().isSave();
    this.publish = request.isPublish();
    this.targetDescriptor = many.getTargetDescriptor();
    this.isMap = many.getManyType().isMap();
    this.saveRecurseSkippable = many.isSaveRecurseSkippable();
    this.deleteMode = targetDescriptor.isSoftDelete() ? DeleteMode.SOFT : DeleteMode.HARD;
  }

  @Override
  void save() {

    if (many.hasJoinTable()) {

      // check if we can save the m2m intersection in this direction
      // we only allow one direction based on first traversed basis
      boolean saveIntersectionFromThisDirection = isSaveIntersection();
      if (cascade) {
        saveAssocManyDetails(false);
      }
      // for ManyToMany save the 'relationship' via inserts/deletes
      // into/from the intersection table
      if (saveIntersectionFromThisDirection) {
        // only allowed on one direction of a m2m based on beanName
        saveAssocManyIntersection(request.isDeleteMissingChildren());
      } else {
        resetModifyState();
      }
    } else {
      if (isModifyListenMode()) {
        // delete any removed beans via private owned. Needs to occur before
        // a 'deleteMissingChildren' statement occurs
        removeAssocManyPrivateOwned();
      }
      if (cascade) {
        // potentially deletes 'missing children' for 'stateless update'
        saveAssocManyDetails(request.isDeleteMissingChildren());
      }
    }
  }

  private boolean isSaveIntersection() {
    return transaction.isSaveAssocManyIntersection(many.getIntersectionTableJoin().getTable(), many.getBeanDescriptor().getName());
  }

  private boolean isModifyListenMode() {
    return BeanCollection.ModifyListenMode.REMOVALS == many.getModifyListenMode();
  }

  /**
   * Save the details from a OneToMany collection.
   */
  private void saveAssocManyDetails(boolean deleteMissingChildren) {

    this.deleteMissing = deleteMissingChildren;

    // check that the list is not null and if it is a BeanCollection
    // check that is has been populated (don't trigger lazy loading)

    collection = BeanCollectionUtil.getActualEntries(value);
    if (collection != null) {
      processDetails();
    }
  }

  private void processDetails() {

    BeanProperty orderColumn = null;
    boolean hasOrderColumn = many.hasOrderColumn();
    if (hasOrderColumn) {
      if (!insertedParent && canSkipForOrderColumn()) {
        return;
      }
      orderColumn = targetDescriptor.getOrderColumn();
    }

    if (insertedParent) {
      // performance optimisation for large collections
      targetDescriptor.preAllocateIds(collection.size());
    }

    if (deleteMissing) {
      // collect the Id's (to exclude from deleteManyDetails)
      List<Object> detailIds = collectIds(collection, targetDescriptor, isMap);
      // deleting missing children - children not in our collected detailIds
      persister.deleteManyDetails(transaction, many.getBeanDescriptor(), parentBean, many, detailIds, deleteMode);
    }

    transaction.depth(+1);
    saveAllBeans(orderColumn);
    if (hasOrderColumn) {
      resetModifyState();
    }
    transaction.depth(-1);
  }


  private void saveAllBeans(BeanProperty orderColumn) {

    // if a map, then we get the key value and
    // set it to the appropriate property on the
    // detail bean before we save it
    Object mapKeyValue = null;
    boolean skipSavingThisBean;

    for (Object detailBean : collection) {
      sortOrder++;
      if (isMap) {
        // its a map so need the key and value
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
          if (orderColumn != null) {
            orderColumn.setValue(detail, sortOrder);
            ebi.setDirty(true);
          }
          if (targetDescriptor.isReference(ebi)) {
            // we can skip this one
            skipSavingThisBean = true;

          } else if (ebi.isNewOrDirty()) {
            skipSavingThisBean = false;
            // set the parent bean to detailBean
            many.setJoinValuesToChild(parentBean, detail, mapKeyValue);

          } else {
            // unmodified so skip depending on prop.isSaveRecurseSkippable();
            skipSavingThisBean = saveRecurseSkippable;
          }
        }

        if (!skipSavingThisBean) {
          persister.saveRecurse(detail, transaction, parentBean, request.getFlags());
        }
      }
    }
  }

  /**
   * Return true if we can skip based on .. no modifications to the collection and no beans are dirty.
   */
  private boolean canSkipForOrderColumn() {
    return value instanceof BeanCollection
      && !((BeanCollection<?>) value).wasTouched()
      && noDirtyBeans();
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
   * Collect the Id values of the details to remove 'missing children' for stateless updates.
   */
  private List<Object> collectIds(Collection<?> collection, BeanDescriptor<?> targetDescriptor, boolean isMap) {

    List<Object> detailIds = new ArrayList<>();
    // stateless update with deleteMissingChildren so first
    // collect the Id values to remove the 'missing children'
    for (Object detailBean : collection) {
      if (isMap) {
        detailBean = ((Map.Entry<?, ?>) detailBean).getValue();
      }
      if (detailBean instanceof EntityBean) {
        Object id = targetDescriptor.getId((EntityBean) detailBean);
        if (!DmlUtil.isNullOrZero(id)) {
          // remember the Id (other details not in the collection) will be removed
          detailIds.add(id);
        }
      }
    }
    return detailIds;
  }

  /**
   * Save the additions and removals from a ManyToMany collection as inserts
   * and deletes from the intersection table.
   * <p>
   * This is done via MapBeans.
   * </p>
   */
  private void saveAssocManyIntersection(boolean deleteMissingChildren) {

    if (value == null) {
      return;
    }

    //SpiTransaction t = saveManyPropRequest.getTransaction();
    boolean vanillaCollection = !(value instanceof BeanCollection<?>);

    if (vanillaCollection || deleteMissingChildren) {
      // delete all intersection rows and then treat all
      // beans in the collection as additions
      persister.deleteAssocManyIntersection(parentBean, many, transaction, publish);
    }

    Collection<?> deletions = null;
    Collection<?> additions;

    if (insertedParent || vanillaCollection || deleteMissingChildren) {
      // treat everything in the list/set/map as an intersection addition
      if (value instanceof Map<?, ?>) {
        additions = ((Map<?, ?>) value).values();
      } else if (value instanceof Collection<?>) {
        additions = (Collection<?>) value;
      } else {
        throw new PersistenceException("Unhandled ManyToMany type " + value.getClass().getName() + " for " + many.getFullBeanName());
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
        additions = manyValue.getActualDetails();
      } else {
        additions = manyValue.getModifyAdditions();
        deletions = manyValue.getModifyRemovals();
      }
      // reset so the changes are only processed once
      manyValue.modifyReset();
    }

    transaction.depth(+1);

    if (additions != null && !additions.isEmpty()) {
      // ensure any cascade batch has been flushed prior
      // to inserting into the intersection table
      transaction.flushBatch();

      for (Object other : additions) {
        EntityBean otherBean = (EntityBean) other;
        // the object from the 'other' side of the ManyToMany
        if (deletions != null && deletions.remove(otherBean)) {
          String m = "Inserting and Deleting same object? " + otherBean;
          if (transaction.isLogSummary()) {
            transaction.logSummary(m);
          }
          log.warn(m);

        } else {
          if (!many.hasImportedId(otherBean)) {
            String msg = "ManyToMany bean " + otherBean + " does not have an Id value.";
            throw new PersistenceException(msg);

          } else {
            // build a intersection row for 'insert'
            IntersectionRow intRow = many.buildManyToManyMapBean(parentBean, otherBean, publish);
            SqlUpdate sqlInsert = intRow.createInsert(server);
            persister.executeSqlUpdate(sqlInsert, transaction);
          }
        }
      }
    }
    if (deletions != null && !deletions.isEmpty()) {
      // ensure any cascade batch has been flushed prior
      // to inserting into the intersection table
      transaction.flushBatch();

      for (Object other : deletions) {
        EntityBean otherDelete = (EntityBean) other;
        // the object from the 'other' side of the ManyToMany
        // build a intersection row for 'delete'
        IntersectionRow intRow = many.buildManyToManyMapBean(parentBean, otherDelete, publish);
        SqlUpdate sqlDelete = intRow.createDelete(server, DeleteMode.HARD);
        persister.executeSqlUpdate(sqlDelete, transaction);
      }
    }

    // decrease the depth back to what it was
    transaction.depth(-1);
  }

  private void removeAssocManyPrivateOwned() {

    // check that the list is not null and if it is a BeanCollection
    // check that is has been populated (don't trigger lazy loading)
    if (value instanceof BeanCollection<?>) {

      BeanCollection<?> c = (BeanCollection<?>) value;
      Set<?> modifyRemovals = c.getModifyRemovals();
      modifyListenReset(c);
      if (modifyRemovals != null && !modifyRemovals.isEmpty()) {
        for (Object removedBean : modifyRemovals) {
          if (removedBean instanceof EntityBean) {
            EntityBean eb = (EntityBean) removedBean;
            if (eb._ebean_getIntercept().isLoaded()) {
              // only delete if the bean was loaded meaning that it is known to exist in the DB
              persister.deleteRequest(persister.createPublishRequest(removedBean, transaction, PersistRequest.Type.DELETE, request.getFlags()));
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
    BeanCollection.ModifyListenMode mode = manyValue.getModifyListening();
    if (mode == null) {
      // new collection persisted for the first time
      manyValue.setModifyListening(prop.getModifyListenMode());
      return true;
    }
    return false;
  }
}
