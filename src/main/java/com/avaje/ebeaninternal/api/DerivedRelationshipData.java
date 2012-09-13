package com.avaje.ebeaninternal.api;

public final class DerivedRelationshipData {

	private final Object assocBean;
	private final String logicalName;
	private final Object bean;
	
	public DerivedRelationshipData(Object assocBean, String logicalName, Object bean) {
	    this.assocBean = assocBean;
	    this.logicalName = logicalName;
	    this.bean = bean;
    }
	
	public Object getAssocBean() {
    	return assocBean;
    }

	public String getLogicalName() {
    	return logicalName;
	}

	public Object getBean() {
    	return bean;
    }
	
}
