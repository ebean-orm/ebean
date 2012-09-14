package com.avaje.tests.xml.runtime;

import com.avaje.ebean.text.StringFormatter;
import com.avaje.ebean.text.StringParser;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssoc;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

public abstract class XrBase {

    protected final String nodeName;
    protected final ElPropertyValue prop;
    protected final StringFormatter stringFormatter;
    protected final StringParser stringParser;

    protected final boolean encode;

    protected boolean decreaseDepth = false;

    protected BeanDescriptor<?> beanDescriptor;

    protected XrBase(String nodeName, ElPropertyValue prop, BeanDescriptor<?> parentDesc, StringFormatter formatter, StringParser parser) {
        
        if (formatter == null && prop != null){
            formatter = prop.getStringFormatter();
        }
        if (parser == null && prop != null){
            parser = prop.getStringParser();
        }
        
        this.nodeName = nodeName;
        this.prop = prop;
        this.stringFormatter = formatter;
        this.stringParser = parser;
        this.encode = false;
        
        this.beanDescriptor = getBeanDescriptor(parentDesc, prop);
    }

    private BeanDescriptor<?> getBeanDescriptor(BeanDescriptor<?> parent, ElPropertyValue prop){
        if (prop != null){
            BeanProperty beanProperty = prop.getBeanProperty();
            if (beanProperty instanceof BeanPropertyAssoc<?>){
                return ((BeanPropertyAssoc<?>)beanProperty).getTargetDescriptor();
            }
        }
        return parent;
    }
    
    protected void setObjectValue(Object bean, Object value){
        prop.elSetValue(bean, value, true, false);
    }
    
    protected Object getObjectValue(Object bean) {
        return prop.elGetValue(bean);
    }

    protected Object getParsedValue(String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        //FIXME xml decode the string
        if (stringParser != null){
            return stringParser.parse(value);
        }
        return value;
    }
    
    protected String getFormattedValue(Object value) {

        if (value == null) {
            return "";
        }
        String s;
        if (stringFormatter == null){
            s = value.toString();
        } else {
            s = stringFormatter.format(value);            
        }
        
        if (encode){
            //FIXME xml encode the string
        }
        
        return s;
    }
}
