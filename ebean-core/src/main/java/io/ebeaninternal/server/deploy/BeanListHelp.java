package io.ebeaninternal.server.deploy;

import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollectionAdd;
import io.ebean.bean.EntityBean;
import io.ebean.common.BeanList;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.json.SpiJsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper object for dealing with Lists.
 */
public class BeanListHelp<T> extends BaseCollectionHelp<T> {

  BeanListHelp(BeanPropertyAssocMany<T> many) {
    super(many);
  }

  BeanListHelp() {
    super();
  }

  @Override
  public BeanCollectionAdd getBeanCollectionAdd(Object bc, String mapKey) {

    if (bc instanceof BeanList<?>) {
      BeanList<?> bl = (BeanList<?>) bc;
      if (bl.getActualList() == null) {
        bl.setActualList(new ArrayList<>());
      }
      return bl;

    } else {
      throw new RuntimeException("Unhandled type " + bc);
    }
  }

  @Override
  public BeanCollection<T> createEmptyNoParent() {
    return new BeanList<>();
  }

  @Override
  public BeanCollection<T> createEmpty(EntityBean parentBean) {
    BeanList<T> beanList = new BeanList<>(loader, parentBean, propertyName);
    if (many != null) {
      beanList.setModifyListening(many.getModifyListenMode());
    }
    return beanList;
  }

  @Override
  public BeanCollection<T> createReference(EntityBean parentBean) {

    BeanList<T> beanList = new BeanList<>(loader, parentBean, propertyName);
    beanList.setModifyListening(many.getModifyListenMode());
    return beanList;
  }

  @Override
  public void refresh(SpiEbeanServer server, Query<?> query, Transaction t, EntityBean parentBean) {

    BeanList<?> newBeanList = (BeanList<?>) server.findList(query, t);
    refresh(newBeanList, parentBean);
  }

  @Override
  public void refresh(BeanCollection<?> bc, EntityBean parentBean) {

    BeanList<?> newBeanList = (BeanList<?>) bc;

    List<?> currentList = (List<?>) many.getValue(parentBean);

    newBeanList.setModifyListening(many.getModifyListenMode());

    if (currentList == null) {
      // the currentList is null? Not really expecting this...
      many.setValue(parentBean, newBeanList);

    } else if (currentList instanceof BeanList<?>) {
      // normally this case, replace just the underlying list
      BeanList<?> currentBeanList = (BeanList<?>) currentList;
      currentBeanList.setActualList(newBeanList.getActualList());
      currentBeanList.setModifyListening(many.getModifyListenMode());

    } else {
      // replace the entire list with the BeanList
      many.setValue(parentBean, newBeanList);
    }
  }

  @Override
  public void jsonWrite(SpiJsonWriter ctx, String name, Object collection, boolean explicitInclude) throws IOException {

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
      list = beanList.getActualList();
    } else {
      list = (List<?>) collection;
    }

    jsonWriteCollection(ctx, name, list);
  }

}
