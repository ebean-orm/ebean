package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.text.StringFormatter;
import com.avaje.ebean.text.StringParser;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

/**
 * Adapter for CtCompoundProperty to ElPropertyValue.
 * <p>
 * This is used for non-scalar properties of a Compound Value Object. These only
 * occur in nested compound types.
 * </p>
 * 
 * @author rbygrave
 */
public class CtCompoundPropertyElAdapter implements ElPropertyValue {

    private final CtCompoundProperty prop;

    private int deployOrder;
    
    public CtCompoundPropertyElAdapter(CtCompoundProperty prop) {
        this.prop = prop;
    }
    
    public void setDeployOrder(int deployOrder) {
        this.deployOrder = deployOrder;
    }

    public Object elConvertType(Object value) {
        return value;
    }

    public Object elGetReference(EntityBean bean) {
        return bean;
    }

    public Object elGetValue(EntityBean bean) {
        return prop.getValue(bean);
    }

    public void elSetReference(EntityBean bean) {
        // Do nothing
    }

    public void elSetValue(EntityBean bean, Object value, boolean populate) {
        prop.setValue(bean, value);
    }

    public int getDeployOrder() {
        return deployOrder;
    }

    public String getAssocOneIdExpr(String prefix, String operator) {
        throw new RuntimeException("Not Supported or Expected");
    }

    public Object[] getAssocOneIdValues(EntityBean bean) {
        throw new RuntimeException("Not Supported or Expected");
    }
    
    public String getAssocIdInExpr(String prefix) {
        throw new RuntimeException("Not Supported or Expected");
    }

    public String getAssocIdInValueExpr(int size) {
        throw new RuntimeException("Not Supported or Expected");
    }

    public BeanProperty getBeanProperty() {
        return null;
    }

    public StringFormatter getStringFormatter() {
        return null;
    }

    public StringParser getStringParser() {
        return null;
    }

    public boolean isDbEncrypted() {
        return false;
    }

    public boolean isLocalEncrypted() {
        return false;
    }

    public boolean isAssocId() {
        return false;
    }
    
    public boolean isAssocProperty() {
        return false;
    }

    public boolean isDateTimeCapable() {
        return false;
    }

    public int getJdbcType() {
	    return 0;
    }

	public Object parseDateTime(long systemTimeMillis) {
        throw new RuntimeException("Not Supported or Expected");
    }
    
    @Override
    public boolean containsFormulaWithJoin() {
      return false;
    }

    public boolean containsMany() {
        return false;
    }

    public boolean containsManySince(String sinceProperty) {
        return containsMany();
    }
    
    public String getDbColumn() {
        return null;
    }

    public String getElPlaceholder(boolean encrypted) {
        return null;
    }

    public String getElPrefix() {
        return null;
    }

    public String getName() {
        return prop.getPropertyName();
    }

    public String getElName() {
        return prop.getPropertyName();
    }

}
