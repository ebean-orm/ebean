package com.avaje.ebeaninternal.server.deploy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.properties.BeanPropertySetter;
import com.avaje.ebeaninternal.server.type.CtCompoundProperty;

/**
 * Represents the root BeanProperty for properties of a compound type.
 * <p>
 * Holds all the scalar and non-scalar properties of the compound type. The
 * scalar properties match to DB columns and the non-scalar ones are here solely
 * to support EL expression language for nested compound types.
 * </p>
 * 
 * @author rbygrave
 */
public class BeanPropertyCompoundRoot {

    private final BeanPropertySetter setter;

    /**
     * The method used to write the property.
     */
    private final Method writeMethod;

    private final String name;
    private final String fullBeanName;

    private final LinkedHashMap<String, BeanPropertyCompoundScalar> propMap;

    private final ArrayList<BeanPropertyCompoundScalar> propList;

    private List<CtCompoundProperty> nonScalarProperties;

    public BeanPropertyCompoundRoot(DeployBeanProperty deploy) {
        this.fullBeanName = deploy.getFullBeanName();
        this.name = deploy.getName();
        this.setter = deploy.getSetter();
        this.writeMethod = deploy.getWriteMethod();
        this.propList = new ArrayList<BeanPropertyCompoundScalar>();
        this.propMap = new LinkedHashMap<String, BeanPropertyCompoundScalar>();
    }

    public BeanProperty[] getScalarProperties() {

        return propList.toArray(new BeanProperty[propList.size()]);
    }

    public void register(BeanPropertyCompoundScalar prop) {
        propList.add(prop);
        propMap.put(prop.getName(), prop);
    }

    public BeanPropertyCompoundScalar getCompoundScalarProperty(String propName) {
        return propMap.get(propName);
    }

    public List<CtCompoundProperty> getNonScalarProperties() {
        return nonScalarProperties;
    }

    public void setNonScalarProperties(List<CtCompoundProperty> nonScalarProperties) {
        this.nonScalarProperties = nonScalarProperties;
    }

    /**
     * Set the value of the property without interception or
     * PropertyChangeSupport.
     */
    public void setRootValue(EntityBean bean, Object value) {
        try {
            if (bean instanceof EntityBean) {
                setter.set(bean, value);
            } else {
                Object[] args = new Object[1];
                args[0] = value;
                writeMethod.invoke(bean, args);
            }
        } catch (Exception ex) {
            String beanType = bean == null ? "null" : bean.getClass().getName();
            String msg = "set " + name + " with arg[" + value + "] on ["+fullBeanName+"] with type[" + beanType + "] threw error";
            throw new RuntimeException(msg, ex);
        }
    }

    /**
     * Set the value of the property.
     */
    public void setRootValueIntercept(EntityBean bean, Object value) {
        try {
            if (bean instanceof EntityBean) {
                setter.setIntercept(bean, value);
            } else {
                Object[] args = new Object[1];
                args[0] = value;
                writeMethod.invoke(bean, args);
            }
        } catch (Exception ex) {
            String beanType = bean == null ? "null" : bean.getClass().getName();
            String msg = "setIntercept " + name + " arg[" + value + "] on ["+fullBeanName+"] with type[" + beanType + "] threw error";
            throw new RuntimeException(msg, ex);
        }
    }
}
