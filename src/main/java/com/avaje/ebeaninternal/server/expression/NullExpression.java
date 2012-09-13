package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;


/**
 * Slightly redundant as Query.setId() ultimately also does the same job.
 */
class NullExpression extends AbstractExpression {

	private static final long serialVersionUID = 4246991057451128269L;
	
	private final boolean notNull;
	
	NullExpression(FilterExprPath pathPrefix, String propertyName, boolean notNull) {
		super(pathPrefix, propertyName);
		this.notNull = notNull;
	}
    
	public void addBindValues(SpiExpressionRequest request) {
		
	}
	
	public void addSql(SpiExpressionRequest request) {
		
        String propertyName = getPropertyName();

	    String nullExpr = notNull ? " is not null " : " is null ";
	    
	    ElPropertyValue prop = getElProp(request);
        if (prop != null && prop.isAssocId()){
            request.append(prop.getAssocOneIdExpr(propertyName, nullExpr));
            return;
        }
	    
		request.append(propertyName).append(nullExpr);
	}
	
	/**
	 * Based on notNull flag and the propertyName.
	 */
	public int queryAutoFetchHash() {
		int hc = NullExpression.class.getName().hashCode();
		hc = hc * 31 + (notNull ? 1 : 0);
		hc = hc * 31 + propName.hashCode();
		return hc;
	}

	public int queryPlanHash(BeanQueryRequest<?> request) {
		return queryAutoFetchHash();
	}
	
	public int queryBindHash() {
		return (notNull ? 1 : 0);
	}
}
