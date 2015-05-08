package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to create a counter version column for Long.
 */
public class GeneratedCounterLong implements GeneratedProperty {

    public GeneratedCounterLong() {

    }

    /**
     * Always returns a 1.
     */
    public Object getInsertValue(BeanProperty prop, EntityBean bean) {
        return Long.valueOf(1);
    }

    /**
     * Increments the current value by one.
     */
    public Object getUpdateValue(BeanProperty prop, EntityBean bean) {
        Long i = (Long) prop.getValue(bean);
        return Long.valueOf(i.longValue() + 1);
    }

    /**
     * Include this in every update.
     */
    public boolean includeInUpdate() {
        return true;
    }

    @Override
    public boolean includeInAllUpdates() {
      return false;
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
