package io.ebeaninternal.server.deploy;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.ebean.PersistenceIOException;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.text.json.ReadJson;
import io.ebeaninternal.server.text.json.SpiJsonWriter;
import io.ebeaninternal.server.type.ScalarType;
import java.io.IOException;

/**
 * Bean descriptor used with ElementCollection (where we don't have a mapped type/class).
 */
class BeanDescriptorElement<T> extends BeanDescriptor<T> {

  private final ScalarType<Object> scalarType;
  private final ElementHelp elementHelp;

  BeanDescriptorElement(BeanDescriptorMap owner, DeployBeanDescriptor<T> deploy, ElementHelp elementHelp) {
    super(owner, deploy);
    this.elementHelp = elementHelp;

    BeanProperty[] props = propertiesNonTransient();
    if (props.length == 1) {
      this.scalarType = props[0].getScalarType();
    } else {
      this.scalarType = null;
    }
  }

  @Override
  public boolean isElementType() {
    return true;
  }

  @Override
  protected EntityBean createPrototypeEntityBean(Class<T> beanType) {
    return new ElementEntityBean(properties);
  }

  @Override
  public void jsonWriteElement(SpiJsonWriter ctx, Object element) {
    try {
      scalarType.jsonWrite(ctx.gen(), element);
    } catch (IOException e) {
      throw new PersistenceIOException(e);
    }
  }

  @Override
  public Object jsonReadCollection(ReadJson readJson, EntityBean parentBean) throws IOException {

    JsonParser parser = readJson.getParser();
    ElementCollector add = elementHelp.createCollector();
    do {
      JsonToken token = parser.nextToken();
      if (JsonToken.VALUE_NULL == token || JsonToken.END_ARRAY == token) {
        break;
      }
      Object element = scalarType.jsonRead(parser);
      add.addElement(element);
    } while (true);

    return add.collection();
  }

}
