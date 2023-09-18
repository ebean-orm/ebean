package io.ebeaninternal.server.deploy;

import io.ebean.Transaction;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollectionAdd;
import io.ebean.bean.EntityBean;
import io.ebean.common.BeanSet;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.json.SpiJsonWriter;

import java.io.IOException;
import java.util.Collections;
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

  @Override
  public final BeanCollectionAdd getBeanCollectionAdd(Object bc, String mapKey) {
    if (bc instanceof BeanSet<?>) {
      BeanSet<?> beanSet = (BeanSet<?>) bc;
      if (beanSet.actualSet() == null) {
        beanSet.setActualSet(new LinkedHashSet<>());
      }
      return beanSet;
    } else {
      throw new RuntimeException("Unhandled type " + bc);
    }
  }

  @Override
  public final Object createEmptyReference() {
    return Collections.EMPTY_SET;
  }

  @Override
  public final BeanCollection<T> createEmptyNoParent() {
    return new BeanSet<>();
  }

  @Override
  public final BeanCollection<T> createEmpty(EntityBean ownerBean) {
    BeanSet<T> beanSet = new BeanSet<>(loader, ownerBean, propertyName);
    if (many != null) {
      beanSet.setModifyListening(many.modifyListenMode());
    }
    return beanSet;
  }

  @Override
  public final BeanCollection<T> createReference(EntityBean parentBean) {
    BeanSet<T> beanSet = new BeanSet<>(loader, parentBean, propertyName);
    beanSet.setModifyListening(many.modifyListenMode());
    return beanSet;
  }

  @Override
  public final void refresh(BeanCollection<?> bc, EntityBean parentBean) {
    BeanSet<?> newBeanSet = (BeanSet<?>) bc;
    Set<?> current = (Set<?>) many.getValue(parentBean);
    newBeanSet.setModifyListening(many.modifyListenMode());
    if (current == null) {
      // the currentList is null?  Not really expecting this...
      many.setValue(parentBean, newBeanSet);

    } else if (current instanceof BeanSet<?>) {
      // normally this case, replace just the underlying list
      BeanSet<?> currentBeanSet = (BeanSet<?>) current;
      currentBeanSet.setActualSet(newBeanSet.actualSet());
      currentBeanSet.setModifyListening(many.modifyListenMode());

    } else {
      // replace the entire set
      many.setValue(parentBean, newBeanSet);
    }
  }

  @Override
  public final void jsonWrite(SpiJsonWriter ctx, String name, Object collection, boolean explicitInclude) throws IOException {
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
      set = bc.actualSet();
    } else {
      set = (Set<?>) collection;
    }
    jsonWriteCollection(ctx, name, set);
  }
}
