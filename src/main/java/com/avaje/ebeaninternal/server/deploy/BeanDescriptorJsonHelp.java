package com.avaje.ebeaninternal.server.deploy;

import java.util.Set;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.json.EJson;
import com.avaje.ebean.text.TextException;
import com.avaje.ebean.text.json.JsonWriteBeanVisitor;
import com.avaje.ebeaninternal.server.text.json.WriteJsonContext;
import com.avaje.ebeaninternal.server.text.json.WriteJsonContext.WriteBeanState;

public class BeanDescriptorJsonHelp<T> {

  private final BeanDescriptor<T> desc;
  private final InheritInfo inheritInfo;
  
  public BeanDescriptorJsonHelp(BeanDescriptor<T> desc) {
    this.desc = desc;
    this.inheritInfo = desc.inheritInfo;
  }


  public void jsonWrite(JsonGenerator ctx, EntityBean bean) {

    if (bean != null) {

      ctx.writeStartObject();
      //WriteBeanState prevState = ctx.pushBeanState(bean);

      if (inheritInfo != null) {
        InheritInfo localInheritInfo = inheritInfo.readType(bean.getClass());
        String discValue = localInheritInfo.getDiscriminatorStringValue();
        String discColumn = localInheritInfo.getDiscriminatorColumn();
        ctx.write(discColumn, discValue);
        //ctx.appendDiscriminator(discColumn, discValue);

        BeanDescriptor<?> localDescriptor = localInheritInfo.getBeanDescriptor();
        localDescriptor.jsonWriteProperties(ctx, bean);

      } else {
        jsonWriteProperties(ctx, bean);
      }

      //ctx.pushPreviousState(prevState);
      //ctx.appendObjectEnd();
      ctx.writeEnd();
    }
  }

  @SuppressWarnings("unchecked")
  private void jsonWriteProperties(JsonGenerator ctx, EntityBean bean) {

    //JsonWriteBeanVisitor<T> beanVisitor = (JsonWriteBeanVisitor<T>) ctx.getBeanVisitor();

    Set<String> props = ctx.getIncludeProperties();

    boolean explicitAllProps;
    if (props == null) {
      explicitAllProps = false;
    } else {
      explicitAllProps = props.contains("*");
      if (explicitAllProps || props.isEmpty()) {
        props = null;
      }
    }

    if (desc.idProperty != null) {
      Object idValue = desc.idProperty.getValue(bean);
      if (idValue != null) {
        if (props == null || props.contains(idProperty.getName())) {
          idProperty.jsonWrite(ctx, bean);
        }
      }
    }

    if (!explicitAllProps && props == null) {
      // just render the loaded properties
      props = ((EntityBean)bean)._ebean_getIntercept().getLoadedPropertyNames();
    }
    if (props != null) {
      // render only the appropriate properties (when not all properties)
      for (String prop : props) {
        BeanProperty p = getBeanProperty(prop);
        if (p != null && !p.isId()) {
          p.jsonWrite(ctx, bean);
        }
      }
    } else {
      if (explicitAllProps || !isReference(bean._ebean_getIntercept())) {
        // render all the properties and invoke lazy loading if required
        for (int j = 0; j < propertiesNonTransient.length; j++) {
          propertiesNonTransient[j].jsonWrite(ctx, bean);
        }
        for (int j = 0; j < propertiesTransient.length; j++) {
          propertiesTransient[j].jsonWrite(ctx, bean);
        }
      }
    }

    if (beanVisitor != null) {
      beanVisitor.visit((T) bean, ctx);
    }
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
  
  @SuppressWarnings("unchecked")
  protected T jsonReadObject(JsonParser parser, String path) {

    EntityBean bean = desc.createEntityBean();
    //ctx.pushBean(bean, path, this);

    do {
     
      if (parser.hasNext()) {
        Event event = parser.next();
        if (Event.KEY_NAME == event) {
          String key = parser.getString();
          BeanProperty p = desc.getBeanProperty(key);
          if (p != null) {
            p.jsonRead(parser, bean);
          
          } else {
            Object rawValue = EJson.parse(parser);           
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
