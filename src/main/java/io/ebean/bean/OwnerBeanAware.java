package io.ebean.bean;

/**
 * Interface for beans that need to know it's parent.
 * @author Roland Praml, FOCONIS AG
 *
 */
public interface OwnerBeanAware {

    void setOwnerBeanInfo(Object parent, String propertyName);

}
