package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollectionAdd;
import com.avaje.ebean.bean.BeanCollectionLoader;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.common.BeanMap;
import com.avaje.ebeaninternal.server.text.json.WriteJson;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Helper specifically for dealing with Maps.
 */
public final class BeanMapHelp<T> implements BeanCollectionHelp<T> {

  private final BeanPropertyAssocMany<T> many;
  private final BeanDescriptor<T> targetDescriptor;
  private final String propertyName;
  private final BeanProperty beanProperty;
  private BeanCollectionLoader loader;

  /**
   * When created for a given query that will return a map.
   */
  public BeanMapHelp(BeanDescriptor<T> targetDescriptor, String mapKey) {
    this.targetDescriptor = targetDescriptor;
    this.beanProperty = targetDescriptor.getBeanProperty(mapKey);
    this.many = null;
    this.propertyName = null;
  }

  /**
   * When help is attached to a specific many property.
   */  public BeanMapHelp(BeanPropertyAssocMany<T> many) {
    this.many = many;
    this.targetDescriptor = many.getTargetDescriptor();
    this.propertyName = many.getName();
    this.beanProperty = targetDescriptor.getBeanProperty(many.getMapKey());
  }

  @Override
  public void setLoader(BeanCollectionLoader loader) {
    this.loader = loader;
  }

  @Override
  @SuppressWarnings("unchecked")
  public BeanCollectionAdd getBeanCollectionAdd(Object bc, String mapKey) {

    if (mapKey == null) {
      mapKey = many.getMapKey();
    }
    BeanProperty beanProp = targetDescriptor.getBeanProperty(mapKey);

    if (bc instanceof BeanMap<?, ?>) {
      BeanMap<Object, Object> bm = (BeanMap<Object, Object>) bc;
      Map<Object, Object> actualMap = bm.getActualMap();
      if (actualMap == null) {
        actualMap = new LinkedHashMap<Object, Object>();
        bm.setActualMap(actualMap);
      }
      return new Adder(beanProp, actualMap);

    } else {
      throw new RuntimeException("Unhandled type " + bc);
    }
  }

  static class Adder implements BeanCollectionAdd {

    private final BeanProperty beanProperty;

    private final Map<Object, Object> map;

    Adder(BeanProperty beanProperty, Map<Object, Object> map) {
      this.beanProperty = beanProperty;
      this.map = map;
    }

    public void addBean(EntityBean bean) {
      Object keyValue = beanProperty.getValue(bean);
      map.put(keyValue, bean);
    }
  }

  @Override
  public BeanCollection<T> createEmptyNoParent() {
    return new BeanMap();
  }

  @Override
  @SuppressWarnings("rawtypes")
  public BeanCollection<T> createEmpty(EntityBean ownerBean) {

    BeanMap beanMap = new BeanMap(loader, ownerBean, propertyName);
    if (many != null) {
      beanMap.setModifyListening(many.getModifyListenMode());
    }
    return beanMap;
  }

  @Override
  public void add(BeanCollection<?> collection, EntityBean bean) {

    Object keyValue = beanProperty.getValueIntercept(bean);
    ((BeanMap<?, ?>) collection).internalPut(keyValue, bean);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public BeanCollection<T> createReference(EntityBean parentBean) {

    BeanMap beanMap = new BeanMap(loader, parentBean, propertyName);
    if (many != null) {
      beanMap.setModifyListening(many.getModifyListenMode());
    }
    return beanMap;
  }

  @Override
  public void refresh(EbeanServer server, Query<?> query, Transaction t, EntityBean parentBean) {
    BeanMap<?, ?> newBeanMap = (BeanMap<?, ?>) server.findMap(query, t);
    refresh(newBeanMap, parentBean);
  }

  @Override
  public void refresh(BeanCollection<?> bc, EntityBean parentBean) {

    BeanMap<?, ?> newBeanMap = (BeanMap<?, ?>) bc;
    Map<?, ?> current = (Map<?, ?>) many.getValue(parentBean);

    newBeanMap.setModifyListening(many.getModifyListenMode());
    if (current == null) {
      // the currentMap is null? Not really expecting this...
      many.setValue(parentBean, newBeanMap);

    } else if (current instanceof BeanMap<?, ?>) {
      // normally this case, replace just the underlying list
      BeanMap<?, ?> currentBeanMap = (BeanMap<?, ?>) current;
      currentBeanMap.setActualMap(newBeanMap.getActualMap());
      currentBeanMap.setModifyListening(many.getModifyListenMode());

    } else {
      // replace the entire set
      many.setValue(parentBean, newBeanMap);
    }
  }

  @Override
  public void jsonWrite(WriteJson ctx, String name, Object collection, boolean explicitInclude) throws IOException {

    Map<?, ?> map;
    if (collection instanceof BeanCollection<?>) {
      BeanMap<?, ?> bc = (BeanMap<?, ?>) collection;
      if (!bc.isPopulated()) {
        if (explicitInclude) {
          // invoke lazy loading as collection
          // is explicitly included in the output
          bc.size();
        } else {
          return;
        }
      }
      map = bc.getActualMap();
    } else {
      map = (Map<?, ?>) collection;
    }

    ctx.writeStartArray(name);
    for (Entry<?, ?> entry : map.entrySet()) {
      //FIXME: json write map key ...
      targetDescriptor.jsonWrite(ctx, (EntityBean) entry.getValue());
    }
    ctx.writeEndArray();
  }

}
