package com.avaje.ebeaninternal.server.deploy;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.text.TextException;
import com.avaje.ebeaninternal.server.text.json.WriteJson;
import com.avaje.ebeaninternal.server.text.json.WriteJson.WriteBean;

public class BeanDescriptorJsonHelp<T> {

  private final BeanDescriptor<T> desc;
  
  private final InheritInfo inheritInfo;
  
  public BeanDescriptorJsonHelp(BeanDescriptor<T> desc) {
    this.desc = desc;
    this.inheritInfo = desc.inheritInfo;
  }
  
  public void jsonWrite(WriteJson writeJson, EntityBean bean, String key) {

//    if (writeJson.hasBean()) {

      writeJson.writeStartObject(key);
      //WriteBeanState prevState = ctx.pushBeanState(bean);

      if (inheritInfo == null) {
        jsonWriteProperties(writeJson, bean);
        
      } else {
        InheritInfo localInheritInfo = inheritInfo.readType(bean.getClass());
        String discValue = localInheritInfo.getDiscriminatorStringValue();
        String discColumn = localInheritInfo.getDiscriminatorColumn();
        writeJson.gen().write(discColumn, discValue);

        BeanDescriptor<?> localDescriptor = localInheritInfo.getBeanDescriptor();
        localDescriptor.jsonWriteProperties(writeJson, bean);
      } 

      //ctx.pushPreviousState(prevState);
      writeJson.gen().writeEnd();
  }

  protected void jsonWriteProperties(WriteJson writeJson, EntityBean bean) {

    
    WriteBean writeBean = writeJson.createWriteBean(desc, bean);
    writeBean.write(writeJson);
  }
  
  
  @SuppressWarnings("unchecked")
  public T jsonRead(JsonParser parser, String path) {
    
    if (!parser.hasNext()) {
      return null;
    }
    Event event = parser.next();
    if (Event.VALUE_NULL == event || Event.END_ARRAY == event) {
      return null;
    }
    if (Event.START_OBJECT != event) {
      throw new RuntimeException("Unexpected token "+event+" - expecting start_object at: "+parser.getLocation());
    }

    if (desc.inheritInfo == null) {
      return jsonReadObject(parser, path);
    } 
    
    // check for the discriminator value to determine the correct sub type
    String discColumn = inheritInfo.getRoot().getDiscriminatorColumn();

    if (!parser.hasNext() || ((event = parser.next()) != Event.KEY_NAME)) {
      String msg = "Error reading inheritance discriminator - expected [" + discColumn + "] but no json key?";
      throw new TextException(msg);        
    }
    
    String propName = parser.getString();      
    if (!propName.equalsIgnoreCase(discColumn)) {
      // just try to assume this is the correct bean type in the inheritance 
      BeanProperty property = desc.getBeanProperty(propName);
      if (property != null) {
        EntityBean bean = desc.createEntityBean();
        property.jsonRead(parser, bean);
        return jsonReadProperties(parser, bean);
      }
      String msg = "Error reading inheritance discriminator, expected property ["+discColumn+"] but got [" + propName + "] ?";
      throw new TextException(msg);        
    }
    
    if (!parser.hasNext() || ((event = parser.next()) != Event.VALUE_STRING)) {
      String msg = "Error reading inheritance discriminator - expected value_string token but got [" + event + "] at ["+parser.getLocation()+"]?";
      throw new TextException(msg);        
    }
      
    String discValue = parser.getString(); 
    
    // determine the sub type for this particular json object
    InheritInfo localInheritInfo = inheritInfo.readType(discValue);
    BeanDescriptor<?> localDescriptor = localInheritInfo.getBeanDescriptor();
    return (T) localDescriptor.jsonReadObject(parser, path);
  }
  
  protected T jsonReadObject(JsonParser parser, String path) {

    EntityBean bean = desc.createEntityBean();
    //ctx.pushBean(bean, path, this);

    return jsonReadProperties(parser, bean);
  }
  
  @SuppressWarnings("unchecked")
  protected T jsonReadProperties(JsonParser parser, EntityBean bean) {

    do {
     
      if (parser.hasNext()) {
        Event event = parser.next();
        if (Event.KEY_NAME == event) {
          String key = parser.getString();
          BeanProperty p = desc.getBeanProperty(key);
          if (p != null) {
            p.jsonRead(parser, bean);
          
          } else {
            //Object rawValue = EJson.parse(parser);           
            // unknown property key ...
            //ctx.readUnmappedJson(propName);
          }
          
        } else if (Event.END_OBJECT == event) {
          break;
          
        } else {
          throw new RuntimeException("Unexpected token "+event+" - expecting key or end_object at: "+parser.getLocation());
        }
      }
      
    } while (true);
    return (T)bean;
  }
    
}
