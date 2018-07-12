package io.ebeaninternal.server.deploy;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.bean.EntityBean;
import io.ebean.text.json.EJson;
import io.ebean.text.json.JsonVersionMigrationHandler;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.api.json.SpiJsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

class BeanDescriptorJsonHelp<T> {

  private final BeanDescriptor<T> desc;

  private final InheritInfo inheritInfo;

  BeanDescriptorJsonHelp(BeanDescriptor<T> desc) {
    this.desc = desc;
    this.inheritInfo = desc.inheritInfo;
  }

  void jsonWrite(SpiJsonWriter writeJson, EntityBean bean, String key) throws IOException {

    writeJson.writeStartObject(key);

    if (inheritInfo == null) {
      writeJson.writeBeanVersion(desc);
      jsonWriteProperties(writeJson, bean);

    } else {
      InheritInfo localInheritInfo = inheritInfo.readType(bean.getClass());
      writeJson.writeBeanVersion(localInheritInfo.desc());

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
      if (dirtyProps[prop.getPropertyIndex()]) {
        prop.jsonWrite(writeJson, bean);
      }
    }
    writeJson.writeEndObject();
  }

  @SuppressWarnings("unchecked")
  T jsonRead(SpiJsonReader jsonRead, String path, boolean withInheritance) throws IOException {

    JsonParser parser = jsonRead.getParser();
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

    JsonVersionMigrationHandler migrationHandler = jsonRead.getVersionMigrationHandler();

    if (desc.inheritInfo == null || !withInheritance) {
      if (migrationHandler != null) {
        // Here we perform the migration for non inherited beans
        ObjectNode node = jsonRead.getObjectMapper().readTree(parser);
        if (node.isNull()) {
          return null;
        }
        node = migrationHandler.migrate(node, jsonRead.getObjectMapper(), desc);
        JsonParser newParser = node.traverse();
        SpiJsonReader newReader = jsonRead.forJson(newParser, false);
        JsonToken nextToken = newParser.nextToken();
        // we must assert that we start with START_OBJECT
        if (nextToken != JsonToken.START_OBJECT) {
          throw new JsonParseException(parser, "Unexpected token " + nextToken + " - expecting start_object", newParser.getCurrentLocation());
        }
        return jsonReadObject(newReader, path);
      } else {
        return jsonReadObject(jsonRead, path);
      }
    }

    ObjectNode node = jsonRead.getObjectMapper().readTree(parser);
    if (node.isNull()) {
      return null;
    }
    if (migrationHandler != null) {
      // Step one on "inherited beans". This migration step may fix a wrong set discriminator value
      node = migrationHandler.migrateRoot(node, jsonRead.getObjectMapper(), desc);
    }

    // check for the discriminator value to determine the correct sub type
    String discColumn = inheritInfo.getRoot().getDiscriminatorColumn();
    JsonNode discNode = node.get(discColumn);
    BeanDescriptor<?> newDesc = desc;
    if (discNode == null || discNode.isNull()) {
      if (desc.isAbstractType()) {
        String msg = "Error reading inheritance discriminator - expected [" + discColumn + "] but no json key?";
        throw new JsonParseException(parser, msg, parser.getCurrentLocation());
      }
    } else {
      newDesc = inheritInfo.readType(discNode.asText()).desc();
    }

    if (migrationHandler != null) {
      // Step two on "inherited beans". Migrate the concrete subtype
      node = migrationHandler.migrate(node, jsonRead.getObjectMapper(), newDesc);
    }

    JsonParser newParser = node.traverse();
    SpiJsonReader newReader = jsonRead.forJson(newParser, false);

    return (T) newDesc.jsonReadObject(newReader, path);
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
      JsonParser parser = readJson.getParser();
      JsonToken event = parser.nextToken();
      if (JsonToken.FIELD_NAME == event) {
        String key = parser.getCurrentName();
        BeanProperty p = desc.getBeanProperty(key);
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
    Object id = desc.beanId(bean);
    if (id != null) {
      // check if the bean has already been loaded
      contextBean = readJson.persistenceContextPutIfAbsent(id, bean, desc);
    }
    if (contextBean == null) {
      readJson.beanVisitor(bean, unmappedProperties);
    }
    if (path != null) {
      readJson.popPath();
    }
    return contextBean == null ? (T) bean : (T) contextBean;
  }

}
