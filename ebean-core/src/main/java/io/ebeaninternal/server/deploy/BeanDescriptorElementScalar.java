package io.ebeaninternal.server.deploy;

import io.avaje.json.JsonReader;
import io.ebean.PersistenceIOException;
import io.ebean.SqlUpdate;
import io.ebean.bean.EntityBean;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.api.json.SpiJsonWriter;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

import java.io.IOException;

/**
 * Bean descriptor used with element collection mapped to a list or set of scalar values.
 */
class BeanDescriptorElementScalar<T> extends BeanDescriptorElement<T> {

  private final ScalarType<Object> scalarType;

  BeanDescriptorElementScalar(BeanDescriptorMap owner, DeployBeanDescriptor<T> deploy, ElementHelp elementHelp) {
    super(owner, deploy, elementHelp);
    this.scalarType = firstBaseScalarType();
  }

  @Override
  public void bindElementValue(SqlUpdate insert, Object value) {
    insert.setParameter(value);
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
  public Object jsonReadCollection(SpiJsonReader readJson, EntityBean parentBean) throws IOException {
    JsonReader parser = readJson.parser();
    ElementCollector add = elementHelp.createCollector();
    parser.beginArray();
    while (parser.hasNextElement()) {
      add.addElement(scalarType.jsonRead(parser));
    }
    parser.endArray();
    return add.collection();
  }

}
