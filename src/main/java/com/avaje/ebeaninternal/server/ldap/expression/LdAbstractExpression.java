/**
 * Copyright (C) 2009 Authors
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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
