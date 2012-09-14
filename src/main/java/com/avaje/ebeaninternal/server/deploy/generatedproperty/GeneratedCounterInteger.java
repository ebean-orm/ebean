package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to create a counter version column for Integer.
 */
public class GeneratedCounterInteger implements GeneratedProperty {

    public GeneratedCounterInteger() {

    }

    /**
     * Always returns a 1.
     */
    public Object getInsertValue(BeanProperty prop, Object bean) {
        return Integer.valueOf(1);
    }

    /**
     * Increments the current value by one.
     */
    public Object getUpdateValue(BeanProperty prop, Object bean) {
        Integer i = (Integer) prop.getValue(bean);
        return Integer.valueOf(i.intValue() + 1);
    }

    /**
     * Include this in every update.
     */
    public boolean includeInUpdate() {
        return true;
    }

    /**
     * Include this in every insert setting initial counter value to 1.
     */
    public boolean includeInInsert() {
        return true;
    }

    public boolean isDDLNotNullable() {
        return true;
    }

}
