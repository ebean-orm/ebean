package io.ebeaninternal.server.deploy;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.bean.EntityBean;
import io.ebean.text.json.EJson;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.api.json.SpiJsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.ebeaninternal.server.persist.DmlUtil.isNullOrZero;

final class BeanDescriptorJsonHelp<T> {

  private final BeanDescriptor<T> desc;

  BeanDescriptorJsonHelp(BeanDescriptor<T> desc) {
    this.desc = desc;
  }

  void jsonWrite(SpiJsonWriter writeJson, EntityBean bean, String key) throws IOException {
    writeJson.writeStartObject(key);
    jsonWriteProperties(writeJson, bean);
    writeJson.writeEndObject();
  }

  void jsonWriteProperties(SpiJsonWriter writeJson, EntityBean bean) {
    writeJson.writeBean(desc, bean);
  }

  void jsonWriteDirty(SpiJsonWriter writeJson, EntityBean bean, boolean[] dirtyProps) throws IOException {
    jsonWriteDirtyProperties(writeJson, bean, dirtyProps);
  }

  void jsonWriteDirtyProperties(SpiJsonWriter writeJson, EntityBean bean, boolean[] dirtyProps) throws IOException {
    writeJson.writeStartObject(null);
    // render the dirty properties
    BeanProperty[] props = desc.propertiesNonTransient();
    for (BeanProperty prop : props) {
      if (dirtyProps[prop.propertyIndex()]) {
        prop.jsonWrite(writeJson, bean);
      }
    }
    writeJson.writeEndObject();
  }

  @SuppressWarnings("unchecked")
  T jsonRead(SpiJsonReader jsonRead, String path, boolean withInheritance, T target) throws IOException {
    JsonParser parser = jsonRead.parser();
    //noinspection StatementWithEmptyBody
    if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
      // start object token read by Jackson already
    } else {
      // check for null or start object
      JsonToken token = parser.nextToken();
      if (JsonToken.VALUE_NULL == token || JsonToken.END_ARRAY == token) {
        return null;
      }
      if (JsonToken.START_OBJECT != token) {
        throw new JsonParseException(parser, "Unexpected token " + token + " - expecting start_object", parser.getCurrentLocation());
      }
    }
    return jsonReadObject(jsonRead, path, target);
  }

  private T jsonReadObject(SpiJsonReader readJson, String path, T target) throws IOException {
    EntityBean bean;
    if (target == null) {
      bean = desc.createEntityBeanForJson();
    } else if (desc.beanType.isInstance(target)) {
      bean = (EntityBean) target;
    } else {
      throw new ClassCastException(target.getClass().getName() + " provided, but " + desc.beanType.getClass().getName() + " expected");
    }
    return jsonReadProperties(readJson, bean, path);
  }

  @SuppressWarnings("unchecked")
  private T jsonReadProperties(SpiJsonReader readJson, EntityBean bean, String path) throws IOException {
    if (path != null) {
      readJson.pushPath(path);
    }
    // unmapped properties, send to JsonReadBeanVisitor later
    Map<String, Object> unmappedProperties = null;
    do {
      JsonParser parser = readJson.parser();
      JsonToken event = parser.nextToken();
      if (JsonToken.FIELD_NAME == event) {
        String key = parser.getCurrentName();
        BeanProperty p = desc.beanProperty(key);
        if (p != null) {
          if (p.isVersion() && readJson.update() ) {
            // skip version prop during update
            p.jsonRead(readJson);
          } else {
            p.jsonRead(readJson, bean);
          }
        } else {
          // read an unmapped property
          if (unmappedProperties == null) {
            unmappedProperties = new LinkedHashMap<>();
          }
          unmappedProperties.put(key, EJson.parse(parser));
        }
      } else if (JsonToken.END_OBJECT == event) {
        break;
      } else {
        throw new RuntimeException("Unexpected token " + event + " - expecting key or end_object at: " + parser.getCurrentLocation());
      }

    } while (true);

    if (unmappedProperties != null) {
      desc.setUnmappedJson(bean, unmappedProperties);
    }
    Object contextBean = null;
    Object id = desc.id(bean);
    if (!isNullOrZero(id)) {
      // check if the bean has already been loaded
      contextBean = readJson.persistenceContextPutIfAbsent(id, bean, desc);
    }
    if (contextBean == null) {
      readJson.beanVisitor(bean, unmappedProperties);
      if (!isNullOrZero(id)) {
        desc.setReferenceIfIdOnly(bean._ebean_getIntercept());
      }
    }
    if (path != null) {
      readJson.popPath();
    }
    return contextBean == null ? (T) bean : (T) contextBean;
  }

}
