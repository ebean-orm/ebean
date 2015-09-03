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
 * Flattens an existing diff map converting assoc one beans into the associated id changes.
 */
public class DiffHelpUpdate {

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
        BeanPropertyAssocOne assoc = (BeanPropertyAssocOne)beanProperty;
        if (!assoc.isEmbedded()) {
          // flatten for assoc one beans
          if (flattened == null) {
            flattened = new LinkedHashMap<String, ValuePair>();
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

  private static void flattenToId(Map<String, ValuePair> flattened, Map.Entry<String, ValuePair> entry, BeanProperty beanProperty, BeanPropertyAssocOne assoc) {

    BeanDescriptor<?> oneDesc = assoc.getTargetDescriptor();

    ValuePair value = entry.getValue();
    Object newId = value.getNewValue() == null ? null :  oneDesc.getId((EntityBean)value.getNewValue());
    Object oldId = value.getOldValue() == null ? null :  oneDesc.getId((EntityBean)value.getOldValue());

    String propName = beanProperty.getName() + "." + oneDesc.getIdProperty().getName();
    flattened.put(propName, new ValuePair(newId, oldId));
  }
}
