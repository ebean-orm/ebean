package com.avaje.ebeaninternal.server.type.reflect;


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ImmutableMeta {

    private final Constructor<?> constructor;
    
    private final Method[] readers;
    
    public ImmutableMeta(Constructor<?> constructor, Method[] readers) {
        this.constructor = constructor;
        this.readers = readers;
    }
    
    
    public Constructor<?> getConstructor() {
        return constructor;
    }

    public Method[] getReaders() {
        return readers;
    }
    
    public boolean isCompoundType() {
        return readers.length > 1;
    }
}
