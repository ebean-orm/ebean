package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to generate values for a property rather than have then set by the user.
 * For example generate the update timestamp when a bean is updated.
 */
public interface GeneratedProperty {

    /**
     * Get the generated insert value for a specific property of a bean.
     */
    public Object getInsertValue(BeanProperty prop, EntityBean bean);

    /**
     * Get the generated update value for a specific property of a bean.
     */
    public Object getUpdateValue(BeanProperty prop, EntityBean bean);

    /**
     * Return true if this should always be includes in an update statement.
     * <p>
     * Used to include GeneratedUpdateTimestamp in dynamic table updates.
     * </p>
     */
    public boolean includeInUpdate();
    
    /**
     * Return true if the property should be included in an update even if
     * it is not loaded (ie. Last Updated Timestamp).
     */
    public boolean includeInAllUpdates();

    /**
     * Return true if this should be included in insert statements.
     */
    public boolean includeInInsert();

    /**
     * Return true if the GeneratedProperty implies the DDL to create the DB
     * column should have a not null constraint.
     */
    public boolean isDDLNotNullable();

}
