package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import java.sql.Timestamp;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to generate a timestamp when a bean is inserted.
 */
public class GeneratedInsertTimestamp implements GeneratedProperty {

    /**
     * Return the current time as a Timestamp.
     */
    public Object getInsertValue(BeanProperty prop, EntityBean bean) {
        return new Timestamp(System.currentTimeMillis());
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
