package io.ebeaninternal.server.deploy;

import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollectionAdd;
import io.ebean.bean.EntityBean;
import io.ebean.common.BeanSet;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.json.SpiJsonWriter;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Helper specifically for dealing with Sets.
 */
public class BeanSetHelp<T> extends BaseCollectionHelp<T> {

  /**
   * When attached to a specific many property.
   */
  BeanSetHelp(BeanPropertyAssocMany<T> many) {
    super(many);
  }

  /**
   * For a query that returns a set.
   */
  BeanSetHelp() {
    super();
  }

  @Override
  public BeanCollectionAdd getBeanCollectionAdd(Object bc, String mapKey) {
    if (bc instanceof BeanSet<?>) {
      BeanSet<?> beanSet = (BeanSet<?>) bc;
      if (beanSet.getActualSet() == null) {
        beanSet.setActualSet(new LinkedHashSet<>());
      }
      return beanSet;

    } else {
      throw new RuntimeException("Unhandled type " + bc);
    }
  }

  @Override
  public BeanCollection<T> createEmptyNoParent() {
    return new BeanSet<>();
  }

  @Override
  public BeanCollection<T> createEmpty(EntityBean ownerBean) {
    BeanSet<T> beanSet = new BeanSet<>(loader, ownerBean, propertyName);
    if (many != null) {
      beanSet.setModifyListening(many.getModifyListenMode());
    }
    return beanSet;
  }

  @Override
  public BeanCollection<T> createReference(EntityBean parentBean) {

    BeanSet<T> beanSet = new BeanSet<>(loader, parentBean, propertyName);
    beanSet.setModifyListening(many.getModifyListenMode());
    return beanSet;
  }

  @Override
  public void refresh(SpiEbeanServer server, Query<?> query, Transaction t, EntityBean parentBean) {

    BeanSet<?> newBeanSet = (BeanSet<?>) server.findSet(query, t);
    refresh(newBeanSet, parentBean);
  }

  @Override
  public void refresh(BeanCollection<?> bc, EntityBean parentBean) {

    BeanSet<?> newBeanSet = (BeanSet<?>) bc;

    Set<?> current = (Set<?>) many.getValue(parentBean);

    newBeanSet.setModifyListening(many.getModifyListenMode());
    if (current == null) {
      // the currentList is null?  Not really expecting this...
      many.setValue(parentBean, newBeanSet);

    } else if (current instanceof BeanSet<?>) {
      // normally this case, replace just the underlying list
      BeanSet<?> currentBeanSet = (BeanSet<?>) current;
      currentBeanSet.setActualSet(newBeanSet.getActualSet());
      currentBeanSet.setModifyListening(many.getModifyListenMode());

    } else {
      // replace the entire set
      many.setValue(parentBean, newBeanSet);
    }
  }

  @Override
  public void jsonWrite(SpiJsonWriter ctx, String name, Object collection, boolean explicitInclude) throws IOException {

    Set<?> set;
    if (collection instanceof BeanCollection<?>) {
      BeanSet<?> bc = (BeanSet<?>) collection;
      if (!bc.isPopulated()) {
        if (explicitInclude) {
          // invoke lazy loading as collection
          // is explicitly included in the output
          bc.size();
        } else {
          return;
        }
      }
      set = bc.getActualSet();
    } else {
      set = (Set<?>) collection;
    }
    jsonWriteCollection(ctx, name, set);
  }
}
