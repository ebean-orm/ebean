package com.avaje.ebeaninternal.server.ldap.expression;

import com.avaje.ebeaninternal.api.ManyWhereJoins;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;

/**
 * Base class for simple expressions.
 * 
 * @author rbygrave
 */
public abstract class LdAbstractExpression implements SpiExpression {

	private static final long serialVersionUID = 4072786211853856174L;
	
	protected final String propertyName;
	
	protected LdAbstractExpression(String propertyName) {
		this.propertyName = propertyName;
	}

	protected String nextParam(SpiExpressionRequest request) {
	    
	    int pos = request.nextParameter();
	    return "{"+(pos-1)+"}";	    
	}
	
	public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

		if (propertyName != null){
			ElPropertyDeploy elProp = desc.getElPropertyDeploy(propertyName);
			if (elProp != null && elProp.containsMany()){
				manyWhereJoin.add(elProp);
			}
		}
	}
	
	protected ElPropertyValue getElProp(SpiExpressionRequest request) {

        return request.getBeanDescriptor().getElGetValue(propertyName);
    }
}
