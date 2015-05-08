package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertySimpleCollection;

public class BeanPropertySimpleCollection<T> extends BeanPropertyAssocMany<T> {

  public BeanPropertySimpleCollection(BeanDescriptorMap owner, BeanDescriptor<?> descriptor, DeployBeanPropertySimpleCollection<T> deploy) {
    super(owner, descriptor, deploy);
  }

  public void initialise() {
    super.initialise();
  }

}
