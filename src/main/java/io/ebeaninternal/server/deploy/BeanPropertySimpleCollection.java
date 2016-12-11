package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.deploy.meta.DeployBeanPropertySimpleCollection;

public class BeanPropertySimpleCollection<T> extends BeanPropertyAssocMany<T> {

  public BeanPropertySimpleCollection(BeanDescriptor<?> descriptor, DeployBeanPropertySimpleCollection<T> deploy) {
    super(descriptor, deploy);
  }

}
