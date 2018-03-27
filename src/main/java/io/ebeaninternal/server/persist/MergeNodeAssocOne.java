package io.ebeaninternal.server.persist;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;

import java.util.Objects;

/**
 * Node for processing merge on ToOne properties.
 */
class MergeNodeAssocOne extends MergeNode {

  private final BeanPropertyAssocOne<?> one;

  MergeNodeAssocOne(String fullPath, BeanPropertyAssocOne<?> property) {
    super(fullPath, property);
    this.one = property;
  }

  public void merge(MergeRequest request) {

    EntityBean entityBean = getEntityBean(request.getBean());
    if (entityBean == null) {
      checkOrphanRemoval(request);

    } else {
      Object beanId = targetDescriptor.getId(entityBean);
      if (beanId == null) {
        checkOrphanRemoval(request);

      } else {
        EntityBean outlineBean = getEntityBean(request.getOutline());
        Object outlineId = (outlineBean == null) ? null : targetDescriptor.getId(outlineBean);
        if (isUpdate(beanId, outlineId, request)) {
          entityBean._ebean_getIntercept().setForceUpdate(true);
          cascade(entityBean, outlineBean, request);
        }
      }
    }
  }

  private void checkOrphanRemoval(MergeRequest request) {
    if (one.isOrphanRemoval()) {
      EntityBean outlineBean = getEntityBean(request.getOutline());
      if (outlineBean != null) {
        request.addDelete(outlineBean);
      }
    }
  }

  private boolean isUpdate(Object beanId, Object outlineId, MergeRequest request) {
    return Objects.equals(beanId, outlineId)
      || !request.isClientGeneratedIds()
      || request.idExists(targetDescriptor.getBeanType(), beanId);
  }

  private EntityBean getEntityBean(Object bean) {
    return (EntityBean) one.getVal(bean);
  }
}
