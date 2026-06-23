package io.ebeaninternal.server.deploy;

import io.avaje.json.JsonReader;
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
    JsonReader parser = readJson.parser();
    ElementCollector add = elementHelp.createCollector();
    parser.beginObject();
    while (parser.hasNextField()) {
      String fieldName = parser.nextField();
      if (stringKey) {
        Object val = readJsonElement(readJson, null, null); // CHECKME: Update existing map entry here?
        add.addKeyValue(fieldName, val);
      } else {
        parser.beginObject();
        parser.nextField();
        Object key = scalarTypeKey.jsonRead(parser);
        parser.nextField();
        Object val = readJsonElement(readJson, null, null); // CHECKME: Update existing map entry here?
        parser.endObject();
        add.addKeyValue(key, val);
      }
    }
    parser.endObject();
    return add.collection();
  }

}
