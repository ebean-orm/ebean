package io.ebeaninternal.server.deploy;

import io.ebean.Transaction;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollectionAdd;
import io.ebean.bean.EntityBean;
import io.ebean.common.BeanList;
import io.ebean.common.BeanListLazyAdd;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.json.SpiJsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper object for dealing with Lists.
 */
public class BeanListHelp<T> extends BaseCollectionHelp<T> {

  private final boolean hasOrderColumn;

  BeanListHelp(BeanPropertyAssocMany<T> many) {
    super(many);
    hasOrderColumn = many.hasOrderColumn();
  }

  @Override
  public final BeanCollectionAdd getBeanCollectionAdd(Object bc, String mapKey) {
    if (bc instanceof BeanList<?>) {
      BeanList<?> bl = (BeanList<?>) bc;
      if (bl.actualList() == null) {
        bl.setActualList(new ArrayList<>());
      }
      return bl;

    } else {
      throw new RuntimeException("Unhandled type " + bc);
    }
  }

  @Override
  public final Object createEmptyReference() {
    return Collections.EMPTY_LIST;
  }

  @Override
  public final BeanCollection<T> createEmptyNoParent() {
    return new BeanList<>();
  }

  @Override
  public final BeanCollection<T> createEmpty(EntityBean parentBean) {
    BeanList<T> beanList;
    if (hasOrderColumn) {
      beanList = new BeanList<>(loader, parentBean, propertyName);
    } else {
      beanList = new BeanListLazyAdd<>(loader, parentBean, propertyName);
    }
    if (many != null) {
      beanList.setModifyListening(many.modifyListenMode());
    }
    return beanList;
  }

  @Override
  public final BeanCollection<T> createReference(EntityBean parentBean) {
    BeanList<T> beanList;
    if (hasOrderColumn) {
      beanList = new BeanList<>(loader, parentBean, propertyName);
    } else {
      beanList = new BeanListLazyAdd<>(loader, parentBean, propertyName);
    }
    beanList.setModifyListening(many.modifyListenMode());
    return beanList;
  }

  @Override
  public final void refresh(BeanCollection<?> bc, EntityBean parentBean) {
    BeanList<?> newBeanList = (BeanList<?>) bc;
    List<?> currentList = (List<?>) many.getValue(parentBean);
    newBeanList.setModifyListening(many.modifyListenMode());
    if (currentList == null) {
      // the currentList is null? Not really expecting this...
      many.setValue(parentBean, newBeanList);

    } else if (currentList instanceof BeanList<?>) {
      // normally this case, replace just the underlying list
      BeanList<?> currentBeanList = (BeanList<?>) currentList;
      currentBeanList.setActualList(newBeanList.actualList());
      currentBeanList.setModifyListening(many.modifyListenMode());

    } else {
      // replace the entire list with the BeanList
      many.setValue(parentBean, newBeanList);
    }
  }

  @Override
  public final void jsonWrite(SpiJsonWriter ctx, String name, Object collection, boolean explicitInclude) throws IOException {
    List<?> list;
    if (collection instanceof BeanCollection<?>) {
      BeanList<?> beanList = (BeanList<?>) collection;
      if (!beanList.isPopulated()) {
        if (explicitInclude) {
          // invoke lazy loading as collection
          // is explicitly included in the output
          beanList.size();
        } else {
          return;
        }
      }
      list = beanList.actualList();
    } else {
      list = (List<?>) collection;
    }
    jsonWriteCollection(ctx, name, list);
  }

}
