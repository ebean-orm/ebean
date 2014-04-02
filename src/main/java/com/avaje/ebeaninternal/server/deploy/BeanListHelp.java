package com.avaje.ebeaninternal.server.deploy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollectionAdd;
import com.avaje.ebean.bean.BeanCollectionLoader;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.common.BeanList;
import com.avaje.ebeaninternal.server.text.json.WriteJsonContext;

/**
 * Helper object for dealing with Lists.
 */
public final class BeanListHelp<T> implements BeanCollectionHelp<T> {

  private final BeanPropertyAssocMany<T> many;
  private final BeanDescriptor<T> targetDescriptor;
  private BeanCollectionLoader loader;

  public BeanListHelp(BeanPropertyAssocMany<T> many) {
    this.many = many;
    this.targetDescriptor = many.getTargetDescriptor();
  }

  public BeanListHelp() {
    this.many = null;
    this.targetDescriptor = null;
  }

  public void setLoader(BeanCollectionLoader loader) {
    this.loader = loader;
  }

  /**
   * Internal add bypassing any modify listening.
   */
  public void add(BeanCollection<?> collection, EntityBean bean) {
    collection.internalAdd(bean);
  }

  public BeanCollectionAdd getBeanCollectionAdd(Object bc, String mapKey) {

    if (bc instanceof BeanList<?>) {

      BeanList<?> bl = (BeanList<?>) bc;
      if (bl.getActualList() == null) {
        bl.setActualList(new ArrayList<Object>());
      }
      return bl;
    } else if (bc instanceof List<?>) {
      return new VanillaAdd((List<?>) bc);

    } else {
      throw new RuntimeException("Unhandled type " + bc);
    }
  }

  @SuppressWarnings("unchecked")
  static class VanillaAdd implements BeanCollectionAdd {

    @SuppressWarnings("rawtypes")
    private final List list;

    private VanillaAdd(List<?> list) {
      this.list = list;
    }

    public void addBean(EntityBean bean) {
      list.add(bean);
    }
  }

  public Iterator<?> getIterator(Object collection) {
    return ((List<?>) collection).iterator();
  }

  public Object createEmpty(boolean vanilla) {
    if (vanilla) {
      return new ArrayList<T>();
    }
    BeanList<T> beanList = new BeanList<T>();
    if (many != null) {
      beanList.setModifyListening(many.getModifyListenMode());
    }
    return beanList;
  }

  public BeanCollection<T> createReference(EntityBean parentBean, String propertyName) {

    BeanList<T> beanList = new BeanList<T>(loader, parentBean, propertyName);
    beanList.setModifyListening(many.getModifyListenMode());
    return beanList;
  }

  public void refresh(EbeanServer server, Query<?> query, Transaction t, EntityBean parentBean) {

    BeanList<?> newBeanList = (BeanList<?>) server.findList(query, t);
    refresh(newBeanList, parentBean);
  }

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

  public void jsonWrite(WriteJsonContext ctx, String name, Object collection, boolean explicitInclude) {

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

    ctx.beginAssocMany(name);
    for (int j = 0; j < list.size(); j++) {
      if (j > 0) {
        ctx.appendComma();
      }
      Object detailBean = list.get(j);
      targetDescriptor.jsonWrite(ctx, (EntityBean)detailBean);
    }
    ctx.endAssocMany();
  }

}
