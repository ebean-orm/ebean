package io.ebeaninternal.server.deploy;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.api.json.SpiJsonWriter;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.type.ScalarType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Bean descriptor used with element collection mapped to Map where key and value are scalar types.
 */
class BeanDescriptorElementScalarMap<T> extends BeanDescriptorElement<T> {

  private final ScalarType scalarTypeKey;
  private final ScalarType scalarTypeVal;
  private final boolean stringKey;

  BeanDescriptorElementScalarMap(BeanDescriptorMap owner, DeployBeanDescriptor<T> deploy, ElementHelp elementHelp) {
    super(owner, deploy, elementHelp);
    BeanProperty[] props = propertiesNonTransient();
    if (props.length != 2) {
      throw new IllegalStateException("Expecting 2 properties for key and value but got " + Arrays.toString(props));
    }
    this.scalarTypeKey = props[0].getScalarType();
    this.scalarTypeVal = props[1].getScalarType();
    this.stringKey = String.class.equals(scalarTypeKey.getType());
  }

  @Override
  @SuppressWarnings("unchecked")
  public void jsonWriteMapEntry(SpiJsonWriter ctx, Map.Entry<?, ?> entry) throws IOException {
    ctx.writeStartObject();
    if (stringKey) {
      Object key = entry.getKey();
      String keyName = (key == null) ? "null" : key.toString();
      ctx.writeFieldName(keyName);
      scalarTypeVal.jsonWrite(ctx.gen(), entry.getValue());
    } else {
      ctx.writeFieldName("key");
      scalarTypeKey.jsonWrite(ctx.gen(), entry.getKey());
      ctx.writeFieldName("value");
      scalarTypeVal.jsonWrite(ctx.gen(), entry.getValue());
    }
    ctx.writeEndObject();
  }

  @Override
  public Object jsonReadCollection(SpiJsonReader readJson, EntityBean parentBean) throws IOException {

    JsonParser parser = readJson.getParser();
    ElementCollector add = elementHelp.createCollector();
    do {
      JsonToken token = parser.nextToken();
      if (token != JsonToken.START_OBJECT) {
        break;
      }
      if (stringKey) {
        String key = parser.nextFieldName();
        parser.nextToken();
        Object val = scalarTypeVal.jsonRead(parser);
        add.addKeyValue(key, val);

      } else {
        parser.nextFieldName();
        Object key = scalarTypeKey.jsonRead(parser);

        parser.nextFieldName();
        Object val = scalarTypeVal.jsonRead(parser);
        add.addKeyValue(key, val);
      }

      token = parser.nextToken();
      if (token != JsonToken.END_OBJECT) {
        break;
      }

    } while (true);

    return add.collection();
  }

}
