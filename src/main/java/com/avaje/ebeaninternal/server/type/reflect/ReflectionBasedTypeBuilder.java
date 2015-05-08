package com.avaje.ebeaninternal.server.type.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.type.ScalarTypeWrapper;
import com.avaje.ebeaninternal.server.type.TypeManager;

public class ReflectionBasedTypeBuilder {

    private final TypeManager typeManager;
    
    public ReflectionBasedTypeBuilder(TypeManager typeManager) {
        this.typeManager = typeManager;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ScalarType<?> buildScalarType(ImmutableMeta meta) {
        
        if (meta.isCompoundType()){
            throw new RuntimeException("Must be scalar");
        }
        
        Constructor<?> constructor = meta.getConstructor();
        
        Class<?> logicalType = constructor.getDeclaringClass();
        
        Method[] readers = meta.getReaders();
        Class<?> returnType = readers[0].getReturnType();
        
        ScalarType<?> scalarType = typeManager.recursiveCreateScalarTypes(returnType);
        
        ReflectionBasedScalarTypeConverter r = new ReflectionBasedScalarTypeConverter(constructor, readers[0]);
        
        ScalarTypeWrapper st = new ScalarTypeWrapper(logicalType, scalarType, r);
        
        return st;
    }
    
    public ReflectionBasedCompoundType buildCompound(ImmutableMeta meta) {
     
        Constructor<?> constructor = meta.getConstructor();

        Method[] readers = meta.getReaders();

        ReflectionBasedCompoundTypeProperty[] props = new ReflectionBasedCompoundTypeProperty[readers.length];
        
        
        for (int i = 0; i < readers.length; i++) {
            Class<?> returnType = readers[i].getReturnType();
            
            // ensure that return type is also a ScalarDataReader
            typeManager.recursiveCreateScalarDataReader(returnType);
            
            String name = getPropertyName(readers[i]);
            
            props[i] = new ReflectionBasedCompoundTypeProperty(name, readers[i], returnType);
        }
        
        return new ReflectionBasedCompoundType(constructor, props);
    }
    
    private String getPropertyName(Method method){
    
        String name = method.getName();
        if (name.startsWith("is")){
            return lowerFirstChar(name.substring(2));
        } else if (name.startsWith("get")){
            return lowerFirstChar(name.substring(3));
        }
        String msg = "Expecting method "+name+" to start with is or get "
            +" so as to follow bean specification?";
        throw new RuntimeException(msg);
    }
    
    private String lowerFirstChar(String name) {
        return Character.toLowerCase(name.charAt(0))+name.substring(1);
    }
}
