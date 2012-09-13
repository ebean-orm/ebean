package com.avaje.ebeaninternal.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParamTypeHelper {

    public enum ManyType {
        LIST, SET, MAP, NONE
    }

    public static class TypeInfo {

        private final ManyType manyType;
        private final Class<?> beanType;

        private TypeInfo(ManyType manyType, Class<?> beanType) {
            this.manyType = manyType;
            this.beanType = beanType;
        }

        public boolean isManyType() {
            return !ManyType.NONE.equals(manyType);
        }

        public ManyType getManyType() {
            return manyType;
        }

        public Class<?> getBeanType() {
            return beanType;
        }

        public String toString() {
            if (isManyType()) {
                return manyType + " " + beanType;
            } else {
                return beanType.toString();
            }
        }

    }

    public static TypeInfo getTypeInfo(Type genericType) {

        if (genericType instanceof ParameterizedType) {
            return getParamTypeInfo((ParameterizedType) genericType);
        }

        Class<?> entityType = getBeanType(genericType);
        if (entityType != null) {
            return new TypeInfo(ManyType.NONE, entityType);
        }
        return null;
    }

    /**
     * For Lists Sets and Maps of beans.
     */
    private static TypeInfo getParamTypeInfo(ParameterizedType paramType) {

        Type rawType = paramType.getRawType();

        ManyType manyType = getManyType(rawType);
        if (ManyType.NONE.equals(manyType)) {
            return null;
        }

        Type[] typeArguments = paramType.getActualTypeArguments();

        if (typeArguments.length == 1) {
            Type argType = typeArguments[0];
            Class<?> beanType = getBeanType(argType);
            if (beanType != null) {
                return new TypeInfo(manyType, beanType);
            }
        }

        return null;
    }

    private static Class<?> getBeanType(Type argType) {
        if (argType instanceof Class<?>) {
            return (Class<?>) argType;
        }
        return null;
    }

    private static ManyType getManyType(Type rawType) {
        if (List.class.equals(rawType)) {
            return ManyType.LIST;
        }
        if (Set.class.equals(rawType)) {
            return ManyType.SET;
        }
        if (Map.class.equals(rawType)) {
            return ManyType.MAP;
        }
        return ManyType.NONE;
    }
}
