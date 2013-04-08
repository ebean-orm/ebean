package com.avaje.ebeaninternal.server.deploy.parse;

import javax.persistence.CascadeType;

import com.avaje.ebeaninternal.server.deploy.BeanCascadeInfo;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

/**
 * Base class for reading deployment annotations.
 */
public abstract class AnnotationParser extends AnnotationBase {

  protected final DeployBeanInfo<?> info;

  protected final DeployBeanDescriptor<?> descriptor;

  protected final Class<?> beanType;

  public AnnotationParser(DeployBeanInfo<?> info) {
    super(info.getUtil());
    this.info = info;
    this.beanType = info.getDescriptor().getBeanType();
    this.descriptor = info.getDescriptor();
  }

  /**
   * read the deployment annotations.
   */
  public abstract void parse();

  /**
   * Helper method to set cascade types to the CascadeInfo on BeanProperty.
   */
  protected void setCascadeTypes(CascadeType[] cascadeTypes, BeanCascadeInfo cascadeInfo) {
    if (cascadeTypes != null && cascadeTypes.length > 0) {
      cascadeInfo.setTypes(cascadeTypes);
    }
  }

}
