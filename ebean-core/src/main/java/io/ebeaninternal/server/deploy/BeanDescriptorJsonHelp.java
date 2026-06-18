package io.ebeaninternal.server.deploy;

import io.avaje.json.JsonReader;
import io.avaje.json.JsonReader.Token;
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
      writeJson.gen().name(discColumn);
      writeJson.gen().value(discValue);
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
  T jsonRead(SpiJsonReader jsonRead, String path, boolean withInheritance, T target) throws IOException {
    JsonReader parser = jsonRead.parser();
    if (parser.isNullValue()) {
      return null;
    }
    Token token = parser.currentToken();
    if (token != Token.BEGIN_OBJECT) {
      throw new IllegalStateException("Unexpected token " + token + " - expecting BEGIN_OBJECT at: " + parser.location());
    }

    if (desc.inheritInfo == null || !withInheritance) {
      return jsonReadObject(jsonRead, path, target);
    }

    Map<String, Object> node = EJson.parseObject(parser);
    if (node == null) {
      return null;
    }

    // check for the discriminator value to determine the correct sub type
    String discColumn = inheritInfo.getRoot().getDiscriminatorColumn();
    Object discValue = node.get(discColumn);
    String rawObject = EJson.write(node);
    SpiJsonReader newReader = jsonRead.forJson(rawObject);
    if (discValue == null) {
      if (!desc.isAbstractType()) {
        return desc.jsonReadObject(newReader, path, target);
      }
      String msg = "Error reading inheritance discriminator - expected [" + discColumn + "] but no json key?";
      throw new IllegalStateException(msg);
    }

    BeanDescriptor<T> inheritDesc = (BeanDescriptor<T>) inheritInfo.readType(String.valueOf(discValue)).desc();
    return inheritDesc.jsonReadObject(newReader, path, target);
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
    JsonReader parser = readJson.parser();
    parser.beginObject();

    // unmapped properties, send to JsonReadBeanVisitor later
    Map<String, Object> unmappedProperties = null;
    while (parser.hasNextField()) {
      String key = parser.nextField();
      BeanProperty p = desc.beanProperty(key);
      if (p != null) {
        if (p.isVersion() && readJson.update()) {
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
    }
    parser.endObject();

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
