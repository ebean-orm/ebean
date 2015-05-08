package com.avaje.ebeaninternal.server.type.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.avaje.ebean.config.ScalarTypeConverter;

@SuppressWarnings({ "rawtypes" })
public class ReflectionBasedScalarTypeConverter implements ScalarTypeConverter {

    private static final Object[] NO_ARGS = new Object[0];
    
    private final Constructor<?> constructor;
    
    private final Method reader;
    
    public ReflectionBasedScalarTypeConverter(Constructor<?> constructor, Method reader) {
        this.constructor = constructor;
        this.reader = reader;
    }
    
    public Object getNullValue() {
        return null;
    }

    public Object unwrapValue(Object beanType) {
        if (beanType == null){
            return null;
        }
        try {
            return reader.invoke(beanType, NO_ARGS);
        } catch (Exception e) {
            String msg = "Error invoking read method "+reader.getName()
                        +" on "+beanType.getClass().getName();
            throw new RuntimeException(msg);
        } 
    }

    public Object wrapValue(Object scalarType) {
        try {
            return constructor.newInstance(scalarType);
        } catch (Exception e) {
            String msg = "Error invoking constructor "+constructor+" with "+scalarType;
            throw new RuntimeException(msg);
        } 
    }

    
}
