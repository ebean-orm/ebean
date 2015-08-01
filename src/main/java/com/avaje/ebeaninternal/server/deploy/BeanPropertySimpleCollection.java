package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertySimpleCollection;

public class BeanPropertySimpleCollection<T> extends BeanPropertyAssocMany<T> {

  public BeanPropertySimpleCollection(BeanDescriptor<?> descriptor, DeployBeanPropertySimpleCollection<T> deploy) {
    super(descriptor, deploy);
  }

}
