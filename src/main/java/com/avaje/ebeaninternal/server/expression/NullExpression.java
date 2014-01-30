package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;


/**
 * Slightly redundant as Query.setId() ultimately also does the same job.
 */
class NullExpression extends AbstractExpression {

	private static final long serialVersionUID = 4246991057451128269L;
	
	private final boolean notNull;
	
	NullExpression(String propertyName, boolean notNull) {
		super(propertyName);
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
	public void queryAutoFetchHash(HashQueryPlanBuilder builder) {
	  builder.add(NullExpression.class).add(notNull).add(propName);
	}

	public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {
		queryAutoFetchHash(builder);
	}
	
	public int queryBindHash() {
		return (notNull ? 1 : 0);
	}
}
