package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import java.util.Date;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Generate a (java.util.Date) Timestamp whenever the bean is inserted or
 * updated.
 */
public class GeneratedUpdateDate implements GeneratedProperty {

    /**
     * Return now as a Timestamp.
     */
    public Object getInsertValue(BeanProperty prop, EntityBean bean) {
        return new Date(System.currentTimeMillis());
    }

    /**
     * Return now as a Timestamp.
     */
    public Object getUpdateValue(BeanProperty prop, EntityBean bean) {
        return new Date(System.currentTimeMillis());
    }

    /**
     * For dynamic table updates make sure this is included.
     */
    public boolean includeInUpdate() {
        return true;
    }
    
    @Override
    public boolean includeInAllUpdates() {
      return true;
    }

    /**
     * Include this in every insert.
     */
    public boolean includeInInsert() {
        return true;
    }

    public boolean isDDLNotNullable() {
        return true;
    }

}
