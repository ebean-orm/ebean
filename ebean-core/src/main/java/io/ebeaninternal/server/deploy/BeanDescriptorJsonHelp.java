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
  private final InheritInfo inheritInfo;

  BeanDescriptorJsonHelp(BeanDescriptor<T> desc) {
    this.desc = desc;
    this.inheritInfo = desc.inheritInfo;
  }

  void jsonWrite(SpiJsonWriter writeJson, EntityBean bean, String key) throws IOException {
    writeJson.writeStartObject(key);
    if (inheritInfo == null) {
      jsonWriteProperties(writeJson, bean);
    } else {
      InheritInfo localInheritInfo = inheritInfo.readType(bean.getClass());
      String discValue = localInheritInfo.getDiscriminatorStringValue();
      String discColumn = localInheritInfo.getDiscriminatorColumn();
      writeJson.gen().writeStringField(discColumn, discValue);
      localInheritInfo.desc().jsonWriteProperties(writeJson, bean);
    }
    writeJson.writeEndObject();
  }

  void jsonWriteProperties(SpiJsonWriter writeJson, EntityBean bean) {
    writeJson.writeBean(desc, bean);
  }

  void jsonWriteDirty(SpiJsonWriter writeJson, EntityBean bean, boolean[] dirtyProps) throws IOException {
    if (inheritInfo == null) {
      jsonWriteDirtyProperties(writeJson, bean, dirtyProps);
    } else {
      desc.descOf(bean.getClass()).jsonWriteDirtyProperties(writeJson, bean, dirtyProps);
    }
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
  T jsonRead(SpiJsonReader jsonRead, String path, boolean withInheritance) throws IOException {
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

    if (desc.inheritInfo == null || !withInheritance) {
      return jsonReadObject(jsonRead, path);
    }

    ObjectNode node = jsonRead.mapper().readTree(parser);
    if (node.isNull()) {
      return null;
    }
    JsonParser newParser = node.traverse();
    SpiJsonReader newReader = jsonRead.forJson(newParser);

    // check for the discriminator value to determine the correct sub type
    String discColumn = inheritInfo.getRoot().getDiscriminatorColumn();
    JsonNode discNode = node.get(discColumn);
    if (discNode == null || discNode.isNull()) {
      if (!desc.isAbstractType()) {
        return desc.jsonReadObject(newReader, path);
      }
      String msg = "Error reading inheritance discriminator - expected [" + discColumn + "] but no json key?";
      throw new JsonParseException(newParser, msg, parser.getCurrentLocation());
    }

    BeanDescriptor<T> inheritDesc = (BeanDescriptor<T>) inheritInfo.readType(discNode.asText()).desc();
    return inheritDesc.jsonReadObject(newReader, path);
  }

  private T jsonReadObject(SpiJsonReader readJson, String path) throws IOException {
    EntityBean bean = desc.createEntityBeanForJson();
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
          p.jsonRead(readJson, bean);
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
