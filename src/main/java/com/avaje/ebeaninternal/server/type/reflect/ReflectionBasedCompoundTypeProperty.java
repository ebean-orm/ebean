package com.avaje.ebeaninternal.server.type.reflect;

import java.lang.reflect.Method;

import com.avaje.ebean.config.CompoundTypeProperty;

@SuppressWarnings({ "rawtypes" })
public class ReflectionBasedCompoundTypeProperty implements CompoundTypeProperty {

    private static final Object[] NO_ARGS = new Object[0];
    
    private final Method reader;
    
    private final String name;
    
    private final Class<?> propertyType;
    
    public ReflectionBasedCompoundTypeProperty(String name, Method reader, Class<?> propertyType) {
        this.name = name;
        this.reader = reader;
        this.propertyType = propertyType;
    }
    
    public String toString() {
        return name;
    }
    
    public int getDbType() {
        return 0;
    }

    public String getName() {
        return name;
    }

    public Object getValue(Object valueObject) {
        
        try {
            return reader.invoke(valueObject, NO_ARGS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
    }

    public Class<?> getPropertyType(){
        return propertyType;
    }
    
    
}
