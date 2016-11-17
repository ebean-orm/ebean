package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.ValuePair;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper to perform a diff given two beans of the same type.
 * <p>
 * This intentionally does not include any OneToMany or ManyToMany properties.
 * </p>
 */
public class DiffHelp {

  private DiffHelp() {
  }

  /**
   * Return a map of the differences between a and b.
   * <p>
   * A and B must be of the same type. B can be null, in which case the 'dirty
   * values' of a is returned.
   * </p>
   * <p>
   * This intentionally does not include as OneToMany or ManyToMany properties.
   * </p>
   */
  public static Map<String, ValuePair> diff(Object newBean, Object oldBean, BeanDescriptor<?> desc) {

    if (!(newBean instanceof EntityBean)) {
      throw new IllegalArgumentException("First bean expected to be an enhanced EntityBean? bean:" + newBean);
    }

    if (oldBean != null) {
      if (!(oldBean instanceof EntityBean)) {
        throw new IllegalArgumentException("Second bean expected to be an enhanced EntityBean? bean:" + oldBean);
      }
      if (!newBean.getClass().isAssignableFrom(oldBean.getClass())) {
        throw new IllegalArgumentException("Second bean not assignable to the first bean?");
      }
    }

    if (oldBean == null) {
      return ((EntityBean) newBean)._ebean_getIntercept().getDirtyValues();
    }

    return desc.diff((EntityBean) newBean, (EntityBean) oldBean);
  }


  /**
   * Flattens an existing diff map converting assoc one beans into the associated id changes.
   */
  public static Map<String, ValuePair> flatten(Map<String, ValuePair> values, BeanDescriptor<?> desc) {

    Map<String, ValuePair> flattened = null;

    Iterator<Map.Entry<String, ValuePair>> iterator = values.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, ValuePair> entry = iterator.next();
      BeanProperty beanProperty = desc.getBeanProperty(entry.getKey());
      if (beanProperty instanceof BeanPropertyAssocMany) {
        // filter out assoc many bean properties
        iterator.remove();

      } else if (beanProperty instanceof BeanPropertyAssocOne) {
        BeanPropertyAssocOne<?> assoc = (BeanPropertyAssocOne<?>) beanProperty;
        if (!assoc.isEmbedded()) {
          // flatten for assoc one beans
          if (flattened == null) {
            flattened = new LinkedHashMap<>();
          }
          flattenToId(flattened, entry, beanProperty, assoc);
          iterator.remove();
        }
      }
    }

    if (flattened != null) {
      values.putAll(flattened);
    }

    return values;
  }

  private static void flattenToId(Map<String, ValuePair> flattened, Map.Entry<String, ValuePair> entry, BeanProperty beanProperty, BeanPropertyAssocOne<?> assoc) {

    BeanDescriptor<?> oneDesc = assoc.getTargetDescriptor();

    ValuePair value = entry.getValue();
    Object newId = value.getNewValue() == null ? null : oneDesc.getId((EntityBean) value.getNewValue());
    Object oldId = value.getOldValue() == null ? null : oneDesc.getId((EntityBean) value.getOldValue());

    String propName = beanProperty.getName() + "." + oneDesc.getIdProperty().getName();
    flattened.put(propName, new ValuePair(newId, oldId));
  }
}
