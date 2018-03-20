package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.deploy.meta.DeployBeanPropertySimpleCollection;

public class BeanPropertySimpleCollection<T> extends BeanPropertyAssocMany<T> {

  private BeanDescriptor<T> elementDescriptor;

  public BeanPropertySimpleCollection(BeanDescriptor<?> descriptor, DeployBeanPropertySimpleCollection<T> deploy) {
    super(descriptor, deploy);
    this.elementDescriptor = deploy.getElementDescriptor();
  }

  @Override
  public void initialise(BeanDescriptorInitContext initContext) {
    super.initialise(initContext);
    if (isElementCollection()) {
      // initialise all non-id properties (we don't have an Id property)
      elementDescriptor.initialiseOther(initContext);
    }
  }

  void initialiseTargetDescriptor(BeanDescriptorInitContext initContext) {
    if (isElementCollection()) {
      targetDescriptor = elementDescriptor;
    } else {
      targetDescriptor = descriptor.getBeanDescriptor(targetType);
    }
  }
}
