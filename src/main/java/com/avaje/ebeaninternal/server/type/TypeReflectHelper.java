package com.avaje.ebeaninternal.server.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeReflectHelper {

    public static Class<?>[] getParams(Class<?> cls, Class<?> matchRawType) {

        Type[] types = getParamType(cls, matchRawType);
        Class<?>[] result = new Class<?>[types.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = getClass(types[i]);
        }
        return result;
    }
    
    public static Class<?> getClass(Type type){
        
        if (type instanceof ParameterizedType){
            return getClass(((ParameterizedType)type).getRawType());
        }
        
        return (Class<?>)type;
    }
    
    private static Type[] getParamType(Class<?> cls, Class<?> matchRawType) {
        
        Type[] gis = cls.getGenericInterfaces();
        for (int i = 0; i < gis.length; i++) {
            Type type = gis[i];
            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                Type rawType = paramType.getRawType();
                if (rawType.equals(matchRawType)) {
                    
                    return paramType.getActualTypeArguments();                    
                }
            }
        }
        return null;
    }
}
