package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to generate a (Long) timestamp when a bean is inserted.
 */
public class GeneratedInsertLong implements GeneratedProperty {

    /**
     * Return the current time as a Timestamp.
     */
    public Object getInsertValue(BeanProperty prop, Object bean) {
        return Long.valueOf(System.currentTimeMillis());
    }

    /**
     * Just returns the beans original insert timestamp value.
     */
    public Object getUpdateValue(BeanProperty prop, Object bean) {
        return prop.getValue(bean);
    }

    /**
     * Return false.
     */
    public boolean includeInUpdate() {
        return false;
    }

    /**
     * Return true.
     */
    public boolean includeInInsert() {
        return true;
    }

    public boolean isDDLNotNullable() {
        return true;
    }

}
