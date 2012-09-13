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
package com.avaje.ebeaninternal.server.expression;

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
public abstract class AbstractExpression implements SpiExpression {

	private static final long serialVersionUID = 4072786211853856174L;
	
	protected final String propName;
	
	protected final FilterExprPath pathPrefix;
	
	protected AbstractExpression(FilterExprPath pathPrefix, String propName) {
		this.pathPrefix = pathPrefix;
	    this.propName = propName;
	}

	public String getPropertyName() {
	    if (pathPrefix == null){
	        return propName;
	    } else {
	        String path = pathPrefix.getPath();
	        if (path == null || path.length() == 0){
	            return propName;
	        } else {
	            return path+"."+propName;
	        }
	    }
	}
	
	public void containsMany(BeanDescriptor<?> desc, ManyWhereJoins manyWhereJoin) {

	    String propertyName = getPropertyName();
		if (propertyName != null){
			ElPropertyDeploy elProp = desc.getElPropertyDeploy(propertyName);
			if (elProp != null && elProp.containsMany()){
			    manyWhereJoin.add(elProp);
			}
		}
	}
	
	protected ElPropertyValue getElProp(SpiExpressionRequest request) {

	    String propertyName = getPropertyName();
        return request.getBeanDescriptor().getElGetValue(propertyName);
    }
}
