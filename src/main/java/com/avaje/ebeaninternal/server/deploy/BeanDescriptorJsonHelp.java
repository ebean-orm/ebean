package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.text.json.EJson;
import com.avaje.ebeaninternal.server.text.json.ReadJson;
import com.avaje.ebeaninternal.server.text.json.WriteJson;
import com.avaje.ebeaninternal.server.text.json.WriteJson.WriteBean;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class BeanDescriptorJsonHelp<T> {

  private final BeanDescriptor<T> desc;
  
  private final InheritInfo inheritInfo;
  
  public BeanDescriptorJsonHelp(BeanDescriptor<T> desc) {
    this.desc = desc;
    this.inheritInfo = desc.inheritInfo;
  }
  
  public void jsonWrite(WriteJson writeJson, EntityBean bean, String key) throws IOException {

    writeJson.writeStartObject(key);

    if (inheritInfo == null) {
      jsonWriteProperties(writeJson, bean);

    } else {
      InheritInfo localInheritInfo = inheritInfo.readType(bean.getClass());
      String discValue = localInheritInfo.getDiscriminatorStringValue();
      String discColumn = localInheritInfo.getDiscriminatorColumn();
      writeJson.gen().writeStringField(discColumn, discValue);

      BeanDescriptor<?> localDescriptor = localInheritInfo.getBeanDescriptor();
      localDescriptor.jsonWriteProperties(writeJson, bean);
    }

    writeJson.writeEndObject();
  }

  protected void jsonWriteProperties(WriteJson writeJson, EntityBean bean) throws IOException {

    
    WriteBean writeBean = writeJson.createWriteBean(desc, bean);
    writeBean.write(writeJson);
  }
  
  
  @SuppressWarnings("unchecked")
  public T jsonRead(ReadJson jsonRead, String path) throws IOException {

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
        throw new JsonParseException("Unexpected token "+token+" - expecting start_object", parser.getCurrentLocation());
      }
    }

    if (desc.inheritInfo == null) {
      return jsonReadObject(jsonRead, path);
    } 
    
    // check for the discriminator value to determine the correct sub type
    String discColumn = inheritInfo.getRoot().getDiscriminatorColumn();

    if (parser.nextToken() != JsonToken.FIELD_NAME) {
      String msg = "Error reading inheritance discriminator - expected [" + discColumn + "] but no json key?";
      throw new JsonParseException(msg, parser.getCurrentLocation());
    }
    
    String propName = parser.getCurrentName();      
    if (!propName.equalsIgnoreCase(discColumn)) {
      // just try to assume this is the correct bean type in the inheritance 
      BeanProperty property = desc.getBeanProperty(propName);
      if (property != null) {
        EntityBean bean = desc.createEntityBean();
        property.jsonRead(jsonRead, bean);
        return jsonReadProperties(jsonRead, bean, path);
      }
      String msg = "Error reading inheritance discriminator, expected property ["+discColumn+"] but got [" + propName + "] ?";
      throw new JsonParseException(msg, parser.getCurrentLocation());
    }
          
    String discValue = parser.nextTextValue(); 
    
    // determine the sub type for this particular json object
    InheritInfo localInheritInfo = inheritInfo.readType(discValue);
    BeanDescriptor<?> localDescriptor = localInheritInfo.getBeanDescriptor();
    return (T) localDescriptor.jsonReadObject(jsonRead, path);
  }
  
  protected T jsonReadObject(ReadJson readJson, String path) throws IOException {

    EntityBean bean = desc.createEntityBean();
    return jsonReadProperties(readJson, bean, path);
  }
  
  @SuppressWarnings("unchecked")
  protected T jsonReadProperties(ReadJson readJson, EntityBean bean, String path) throws IOException {

    if (path != null) {
      readJson.pushPath(path);
    }

    // unmapped properties, send to JsonReadBeanVisitor later
    Map<String,Object> unmappedProperties = null;

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
            unmappedProperties = new LinkedHashMap<String, Object>();
          }
          unmappedProperties.put(key, EJson.parse(parser));
        }

      } else if (JsonToken.END_OBJECT == event) {
        break;

      } else {
        throw new RuntimeException("Unexpected token " + event + " - expecting key or end_object at: " + parser.getCurrentLocation());
      }
      
    } while (true);

    // visit JsonReadBeanVisitor (if registered for this path)
    readJson.beanVisitor(bean, unmappedProperties);

    if (path != null) {
      readJson.popPath();
    }
    return (T)bean;
  }
    
}
