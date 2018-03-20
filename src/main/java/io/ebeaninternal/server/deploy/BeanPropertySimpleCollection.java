package io.ebeaninternal.server.deploy;

import io.ebean.PersistenceIOException;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertySimpleCollection;
import io.ebeaninternal.server.text.json.ReadJson;
import io.ebeaninternal.server.text.json.SpiJsonWriter;

import java.io.IOException;

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

  /**
   * Json write scalar value.
   */
  @Override
  public void jsonWriteValue(SpiJsonWriter writeJson, Object value) {
    try {
      scalarType.jsonWrite(writeJson.gen(), value);
    } catch (IOException e) {
      throw new PersistenceIOException(e);
    }
  }

  public void jsonWriteElementValue(SpiJsonWriter ctx, Object element) {
    elementDescriptor.jsonWriteElement(ctx, element);
  }

  @Override
  public Object jsonReadCollection(ReadJson readJson, EntityBean parentBean) throws IOException {
    return elementDescriptor.jsonReadCollection(readJson, parentBean);
  }
}
