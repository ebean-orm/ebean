package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import java.util.Date;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to generate a (java.util.Date) timestamp when a bean is inserted.
 */
public class GeneratedInsertDate implements GeneratedProperty {

    /**
     * Return the current time as a Timestamp.
     */
    public Object getInsertValue(BeanProperty prop, EntityBean bean) {
        return new Date(System.currentTimeMillis());
    }

    /**
     * Just returns the beans original insert timestamp value.
     */
    public Object getUpdateValue(BeanProperty prop, EntityBean bean) {
        return prop.getValue(bean);
    }

    /**
     * Return false.
     */
    public boolean includeInUpdate() {
        return false;
    }

    @Override
    public boolean includeInAllUpdates() {
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
