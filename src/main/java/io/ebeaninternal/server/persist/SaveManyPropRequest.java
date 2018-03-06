package io.ebeaninternal.server.persist;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanCollectionUtil;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.ManyType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Helper to wrap the details when saving a OneToMany or ManyToMany relationship.
 */
class SaveManyPropRequest {

  private final boolean insertedParent;
  private final BeanPropertyAssocMany<?> many;
  private final EntityBean parentBean;
  private final SpiTransaction transaction;
  private final boolean cascade;
  private final boolean deleteMissingChildren;
  private final boolean publish;

  private final Object value;
  private final BeanDescriptor<?> targetDescriptor;
  private final boolean isMap;
  private final boolean saveRecurseSkippable;

  private Collection<?> collection;
  private DefaultPersister persister;
  private boolean deleteMissing;
  private boolean insertMode;
  private int sortOrder;

  SaveManyPropRequest(boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
    this.insertedParent = insertedParent;
    this.many = many;
    this.cascade = many.getCascadeInfo().isSave();
    this.parentBean = parentBean;
    this.transaction = request.getTransaction();
    this.deleteMissingChildren = request.isDeleteMissingChildren();
    this.publish = request.isPublish();
    this.value = many.getValue(parentBean);
    this.targetDescriptor = many.getTargetDescriptor();
    this.isMap = ManyType.MAP == many.getManyType();
    this.saveRecurseSkippable = many.isSaveRecurseSkippable();
  }

  public boolean isSaveIntersection() {
    return transaction.isSaveAssocManyIntersection(many.getIntersectionTableJoin().getTable(), many.getBeanDescriptor().getName());
  }

  Object getValue() {
    return value;
  }

  boolean isModifyListenMode() {
    return BeanCollection.ModifyListenMode.REMOVALS == many.getModifyListenMode();
  }

  boolean isDeleteMissingChildren() {
    return deleteMissingChildren;
  }

  boolean isInsertedParent() {
    return insertedParent;
  }

  BeanPropertyAssocMany<?> getMany() {
    return many;
  }

  EntityBean getParentBean() {
    return parentBean;
  }

  SpiTransaction getTransaction() {
    return transaction;
  }

  boolean isCascade() {
    return cascade;
  }

  boolean isPublish() {
    return publish;
  }

  void modifyListenReset(BeanCollection<?> c) {
    if (insertedParent) {
      // after insert set the modify listening mode for private owned etc
      c.setModifyListening(many.getModifyListenMode());
    }
    c.modifyReset();
  }

  void resetModifyState() {
    if (value instanceof BeanCollection<?>) {
      modifyListenReset((BeanCollection<?>) value);
    }
  }

  void saveDetails(DefaultPersister persister, boolean deleteMissing, boolean insertMode) {

    this.persister = persister;
    this.deleteMissing = deleteMissing;
    this.insertMode = insertMode;

    // check that the list is not null and if it is a BeanCollection
    // check that is has been populated (don't trigger lazy loading)
    // For a Map this is a collection of Map.Entry objects and not beans
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
      persister.deleteManyDetails(transaction, many.getBeanDescriptor(), parentBean, many, detailIds, false);
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
          persister.saveRecurse(detail, transaction, parentBean, insertMode, publish);
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
}
