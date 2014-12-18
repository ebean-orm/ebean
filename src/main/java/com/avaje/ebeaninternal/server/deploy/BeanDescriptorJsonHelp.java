package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.text.json.EJson;
import com.avaje.ebeaninternal.server.text.json.WriteJson;
import com.avaje.ebeaninternal.server.text.json.WriteJson.WriteBean;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

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
  public T jsonRead(JsonParser parser, String path) throws IOException {

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
      return jsonReadObject(parser, path);
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
        property.jsonRead(parser, bean);
        return jsonReadProperties(parser, bean);
      }
      String msg = "Error reading inheritance discriminator, expected property ["+discColumn+"] but got [" + propName + "] ?";
      throw new JsonParseException(msg, parser.getCurrentLocation());
    }
          
    String discValue = parser.nextTextValue(); 
    
    // determine the sub type for this particular json object
    InheritInfo localInheritInfo = inheritInfo.readType(discValue);
    BeanDescriptor<?> localDescriptor = localInheritInfo.getBeanDescriptor();
    return (T) localDescriptor.jsonReadObject(parser, path);
  }
  
  protected T jsonReadObject(JsonParser parser, String path) throws IOException {

    EntityBean bean = desc.createEntityBean();
    return jsonReadProperties(parser, bean);
  }
  
  @SuppressWarnings("unchecked")
  protected T jsonReadProperties(JsonParser parser, EntityBean bean) throws IOException {

    do {

      JsonToken event = parser.nextToken();
      if (JsonToken.FIELD_NAME == event) {
        String key = parser.getCurrentName();
        BeanProperty p = desc.getBeanProperty(key);
        if (p != null) {
          p.jsonRead(parser, bean);
        } else {
          // unknown property ... read and ignore
          EJson.parse(parser);
        }

      } else if (JsonToken.END_OBJECT == event) {
        break;

      } else {
        throw new RuntimeException("Unexpected token " + event + " - expecting key or end_object at: " + parser.getCurrentLocation());
      }
      
    } while (true);
    return (T)bean;
  }
    
}
