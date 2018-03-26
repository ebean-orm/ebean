package io.ebeaninternal.server.deploy;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.text.json.ReadJson;
import io.ebeaninternal.server.text.json.SpiJsonWriter;
import io.ebeaninternal.server.type.ScalarType;
import java.io.IOException;
import java.util.Map;

/**
 * Bean descriptor used with ElementCollection (where we don't have a mapped type/class).
 */
class BeanDescriptorElementMap<T> extends BeanDescriptor<T> {

  private final ScalarType[] scalarTypes;
  private final ElementHelp elementHelp;
  private final boolean stringKey;

  BeanDescriptorElementMap(BeanDescriptorMap owner, DeployBeanDescriptor<T> deploy, ElementHelp elementHelp) {
    super(owner, deploy);
    this.elementHelp = elementHelp;
    BeanProperty[] props = propertiesNonTransient();
    this.scalarTypes = new ScalarType[props.length];
    for (int i = 0; i < props.length; i++) {
      scalarTypes[i] = props[i].getScalarType();
    }
    this.stringKey = String.class.equals(scalarTypes[0].getType());
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
  @SuppressWarnings("unchecked")
  public void jsonWriteMapEntry(SpiJsonWriter ctx, Map.Entry<?, ?> entry) throws IOException {
      ctx.writeStartObject();
      if (stringKey) {
        Object key = entry.getKey();
        String keyName = (key == null) ? "null" : key.toString();
        ctx.writeFieldName(keyName);
        scalarTypes[1].jsonWrite(ctx.gen(), entry.getValue());
      } else {
        ctx.writeFieldName("key");
        scalarTypes[0].jsonWrite(ctx.gen(), entry.getKey());
        ctx.writeFieldName("value");
        scalarTypes[1].jsonWrite(ctx.gen(), entry.getValue());
      }
      ctx.writeEndObject();
  }

  @Override
  public Object jsonReadCollection(ReadJson readJson, EntityBean parentBean) throws IOException {

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
        Object val = scalarTypes[0].jsonRead(parser);
        add.addKeyValue(key, val);

      } else {
        parser.nextFieldName();
        Object key= scalarTypes[0].jsonRead(parser);

        parser.nextFieldName();
        Object val = scalarTypes[0].jsonRead(parser);
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
