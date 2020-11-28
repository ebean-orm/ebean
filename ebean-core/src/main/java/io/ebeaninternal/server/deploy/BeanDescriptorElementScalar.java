package io.ebeaninternal.server.deploy;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
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

    JsonParser parser = readJson.getParser();
    ElementCollector add = elementHelp.createCollector();
    do {
      JsonToken token = parser.nextToken();
      if (JsonToken.VALUE_NULL == token || JsonToken.END_ARRAY == token) {
        break;
      }
      add.addElement(scalarType.jsonRead(parser));
    } while (true);

    return add.collection();
  }

}
