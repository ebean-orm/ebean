package io.ebeaninternal.server.persist;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Node for processing merge on ToMany properties.
 */
class MergeNodeAssocMany extends MergeNode {

  private final BeanPropertyAssocMany<?> many;

  MergeNodeAssocMany(String fullPath, BeanPropertyAssocMany<?> property) {
    super(fullPath, property);
    this.many = property;
  }

  public void merge(MergeRequest request) {

    Collection beans = many.getRawCollection(request.getBean());
    Collection outlines = many.getRawCollection(request.getOutline());

    Map<Object, EntityBean> outlineIds = new HashMap<>();
    if (outlines != null) {
      for (Object outline : outlines) {
        EntityBean outlineBean = (EntityBean) outline;
        Object outlineId = targetDescriptor.getId(outlineBean);
        outlineIds.put(outlineId, outlineBean);
      }
    }

    if (beans != null) {
      for (Object bean : beans) {
        EntityBean entityBean = (EntityBean) bean;
        Object beanId = targetDescriptor.getId(entityBean);
        if (beanId != null) {
          EntityBean outlineBean = outlineIds.remove(beanId);
          if (outlineBean != null) {
            // must be an update
            entityBean._ebean_getIntercept().setForceUpdate(true);
            cascade(entityBean, outlineBean, request);
          }
        }
      }
    }

    // any remaining are considered deletes
    for (EntityBean outlineBean : outlineIds.values()) {
      request.addDelete(outlineBean);
    }
  }

}
