package io.ebeaninternal.server.deploy;

import io.ebean.Transaction;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollectionAdd;
import io.ebean.bean.EntityBean;
import io.ebean.common.BeanMap;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.json.SpiJsonWriter;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Helper specifically for dealing with Maps.
 */
public class BeanMapHelp<T> extends BaseCollectionHelp<T> {

  private final BeanProperty beanProperty;

  /**
   * When help is attached to a specific many property.
   */
  BeanMapHelp(BeanPropertyAssocMany<T> many) {
    super(many);
    this.beanProperty = targetDescriptor.beanProperty(many.mapKey());
  }

  @Override
  @SuppressWarnings("unchecked")
  public final BeanCollectionAdd getBeanCollectionAdd(Object bc, String mapKey) {
    if (mapKey == null) {
      mapKey = many.mapKey();
    }
    BeanProperty beanProp = targetDescriptor.beanProperty(mapKey);
    if (bc instanceof BeanMap<?, ?>) {
      BeanMap<Object, Object> bm = (BeanMap<Object, Object>) bc;
      Map<Object, Object> actualMap = bm.actualMap();
      if (actualMap == null) {
        actualMap = new LinkedHashMap<>();
        bm.setActualMap(actualMap);
      }
      return new Adder(beanProp, actualMap);

    } else {
      throw new RuntimeException("Unhandled type " + bc);
    }
  }

  static final class Adder implements BeanCollectionAdd {

    private final BeanProperty beanProperty;
    private final Map<Object, Object> map;

    Adder(BeanProperty beanProperty, Map<Object, Object> map) {
      this.beanProperty = beanProperty;
      this.map = map;
    }

    @Override
    public void addEntityBean(EntityBean bean) {
      Object keyValue = beanProperty.getValue(bean);
      map.put(keyValue, bean);
    }
  }

  @Override
  public final Object createEmptyReference() {
    return Collections.EMPTY_MAP;
  }

  @Override
  public final BeanCollection<T> createEmptyNoParent() {
    return new BeanMap<>();
  }

  @Override
  public final BeanCollection<T> createEmpty(EntityBean ownerBean) {
    BeanMap<?, T> beanMap = new BeanMap<>(loader, ownerBean, propertyName);
    if (many != null) {
      beanMap.setModifyListening(many.modifyListenMode());
    }
    return beanMap;
  }

  @Override
  public void add(BeanCollection<?> collection, EntityBean bean, boolean withCheck) {
    if (bean == null) {
      ((BeanMap<?, ?>) collection).internalPutNull();
    } else {
      Object keyValue = beanProperty.getValueIntercept(bean);
      BeanMap<?, ?> map = ((BeanMap<?, ?>) collection);
      map.internalPutWithCheck(keyValue, bean);
    }
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public final BeanCollection<T> createReference(EntityBean parentBean) {
    BeanMap beanMap = new BeanMap(loader, parentBean, propertyName);
    if (many != null) {
      beanMap.setModifyListening(many.modifyListenMode());
    }
    return beanMap;
  }

  @Override
  public final void refresh(BeanCollection<?> bc, EntityBean parentBean) {
    BeanMap<?, ?> newBeanMap = (BeanMap<?, ?>) bc;
    Map<?, ?> current = (Map<?, ?>) many.getValue(parentBean);

    newBeanMap.setModifyListening(many.modifyListenMode());
    if (current == null) {
      // the currentMap is null? Not really expecting this...
      many.setValue(parentBean, newBeanMap);

    } else if (current instanceof BeanMap<?, ?>) {
      // normally this case, replace just the underlying list
      BeanMap<?, ?> currentBeanMap = (BeanMap<?, ?>) current;
      currentBeanMap.setActualMap(newBeanMap.actualMap());
      currentBeanMap.setModifyListening(many.modifyListenMode());

    } else {
      // replace the entire set
      many.setValue(parentBean, newBeanMap);
    }
  }

  @Override
  public final void jsonWrite(SpiJsonWriter ctx, String name, Object collection, boolean explicitInclude) throws IOException {
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
      map = bc.actualMap();
    } else {
      map = (Map<?, ?>) collection;
    }

    if (!map.isEmpty() || ctx.isIncludeEmpty()) {
      final boolean elementCollection = many.isElementCollection();
      ctx.beginAssocManyMap(name, elementCollection);
      for (Entry<?, ?> entry : map.entrySet()) {
        many.jsonWriteMapEntry(ctx, entry);
      }
      ctx.endAssocManyMap(elementCollection);
    }
  }

}
