package com.avaje.ebeaninternal.server.core;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;

import com.avaje.ebean.BeanState;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;

/**
 * Default implementation of BeanState.
 */
public class DefaultBeanState implements BeanState {

	final EntityBean entityBean;
	
	final EntityBeanIntercept intercept;
	
	public DefaultBeanState(EntityBean  entityBean){
		this.entityBean = entityBean;
		this.intercept = entityBean._ebean_getIntercept();
	}

	public boolean isReference() {
		return intercept.isReference();
	}

	public boolean isNew() {
		return intercept.isNew();
	}

	public boolean isNewOrDirty() {
		return intercept.isNewOrDirty();
	}

	public boolean isDirty() {
		return intercept.isDirty();
	}
	
	public Set<String> getLoadedProps() {
	    Set<String> props = intercept.getLoadedProps();
	    return props == null ? null : Collections.unmodifiableSet(props);
	}
	
	public Set<String> getChangedProps() {
	    Set<String> props = intercept.getChangedProps();
        return props == null ? null : Collections.unmodifiableSet(props);
    }
	
	public boolean isReadOnly() {
		return intercept.isReadOnly();
	}
	
	public void setReadOnly(boolean readOnly){
		intercept.setReadOnly(readOnly);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		entityBean.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		entityBean.removePropertyChangeListener(listener);
	}

	public void setLoaded(Set<String> loadedProperties) {
		intercept.setLoadedProps(loadedProperties);
		intercept.setLoaded();
	}

	public void setReference() {
		intercept.setReference();
	}
	
	
}
