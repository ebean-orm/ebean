package io.ebeaninternal.server.deploy;

import com.fasterxml.jackson.core.JsonParser;
import io.ebean.bean.EntityBean;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.api.json.SpiJsonWriter;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

import java.io.IOException;
import java.util.Map;

/**
 * Descriptor for element collection using Map with the value holding an embedded bean.
 * <p>
 * The expected limitation is that the key is a scalar type.
 */
class BeanDescriptorElementEmbeddedMap<T> extends BeanDescriptorElementEmbedded<T> {

  private final ScalarType scalarTypeKey;
  private final boolean stringKey;

  BeanDescriptorElementEmbeddedMap(BeanDescriptorMap owner, DeployBeanDescriptor<T> deploy, ElementHelp elementHelp) {
    super(owner, deploy, elementHelp);
    this.scalarTypeKey = firstBaseScalarType();
    this.stringKey = String.class.equals(scalarTypeKey.type());
  }

  @Override
  public boolean isJsonReadCollection() {
    return true;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void jsonWriteMapEntry(SpiJsonWriter ctx, Map.Entry<?, ?> entry) throws IOException {
    if (stringKey) {
      Object key = entry.getKey();
      String keyName = (key == null) ? "null" : key.toString();
      ctx.writeFieldName(keyName);
      writeJsonElement(ctx, entry.getValue());
    } else {
      ctx.writeFieldName("key");
      scalarTypeKey.jsonWrite(ctx.gen(), entry.getKey());
      ctx.writeFieldName("value");
      writeJsonElement(ctx, entry.getValue());
    }
  }

  @Override
  public Object jsonReadCollection(SpiJsonReader readJson, EntityBean parentBean) throws IOException {
    JsonParser parser = readJson.parser();
    ElementCollector add = elementHelp.createCollector();
    do {
      String fieldName = parser.nextFieldName();
      if (fieldName == null) {
        break;
      }
      if (stringKey) {
        parser.nextToken();
        Object val = readJsonElement(readJson, null);
        add.addKeyValue(fieldName, val);
      } else {
        parser.nextFieldName();
        Object key = scalarTypeKey.jsonRead(parser);
        parser.nextFieldName();
        Object val = readJsonElement(readJson, null);
        add.addKeyValue(key, val);
      }
    } while (true);
    return add.collection();
  }

}
