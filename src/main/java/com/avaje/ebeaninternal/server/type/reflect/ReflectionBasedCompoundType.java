package com.avaje.ebeaninternal.server.type.reflect;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import com.avaje.ebean.config.CompoundType;
import com.avaje.ebean.config.CompoundTypeProperty;

@SuppressWarnings({ "rawtypes" })
public class ReflectionBasedCompoundType implements CompoundType {

    private final Constructor<?> constructor;
    
    private final ReflectionBasedCompoundTypeProperty[] props;
    
    public ReflectionBasedCompoundType(Constructor<?> constructor, ReflectionBasedCompoundTypeProperty[] props) {
        this.constructor = constructor;
        this.props = props;
    }
    
    public String toString() {
        return "ReflectionBasedCompoundType "+constructor+" "+Arrays.toString(props);
    }
    
    public Object create(Object[] propertyValues) {
        
        try {
            return constructor.newInstance(propertyValues);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
    }

    public CompoundTypeProperty[] getProperties() {
        return props;
    }

    public Class<?> getPropertyType(int i){
        return props[i].getPropertyType();
    }
    
    public Class<?> getCompoundType() {
        return constructor.getDeclaringClass();
    }
    
}
