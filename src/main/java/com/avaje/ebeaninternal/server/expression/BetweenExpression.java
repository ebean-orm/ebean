package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.event.BeanQueryRequest;
import com.avaje.ebeaninternal.api.HashQueryPlanBuilder;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;


class BetweenExpression extends AbstractExpression {

	private static final long serialVersionUID = 2078918165221454910L;

	private static final String BETWEEN = " between ";
	
	private final Object valueHigh;
	
	private final Object valueLow;
	
	BetweenExpression(String propertyName, Object valLo, Object valHigh) {
		super(propertyName);
		this.valueLow = valLo;
		this.valueHigh = valHigh;
	}

	public void addBindValues(SpiExpressionRequest request) {
		request.addBindValue(valueLow);
		request.addBindValue(valueHigh);
	}

	public void addSql(SpiExpressionRequest request) {
		
		request.append(getPropertyName()).append(BETWEEN).append(" ? and ? ");
	}

	public void queryAutoFetchHash(HashQueryPlanBuilder builder) {
	  builder.add(BetweenExpression.class).add(propName);
	  builder.bind(2);
	}
	
	public void queryPlanHash(BeanQueryRequest<?> request, HashQueryPlanBuilder builder) {
		queryAutoFetchHash(builder);
	}
	
	public int queryBindHash() {
		int hc = valueLow.hashCode();
		hc = hc * 31 + valueHigh.hashCode();
		return hc;
	}
}
