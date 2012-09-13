/**
 * Copyright (C) 2006  Robin Bygrave
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
package com.avaje.ebeaninternal.server.el;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility object used to build a ElPropertyChain.
 * <p>
 * Builds a ElPropertyChain based on a chain of properties with dot separators.
 * </p>
 * <p>
 * This can navigate an object graph based on dot notation such as
 * order.customer.name.
 * </p>
 */
public class ElPropertyChainBuilder {

	private final String expression;

	private final List<ElPropertyValue> chain = new ArrayList<ElPropertyValue>();

	private final boolean embedded;
	
	private boolean containsMany;
	
	/**
	 * Create with the original expression.
	 */
	public ElPropertyChainBuilder(boolean embedded, String expression) {
		this.embedded = embedded;
		this.expression = expression;
	}
	
	public boolean isContainsMany() {
		return containsMany;
	}

	public void setContainsMany(boolean containsMany) {
		this.containsMany = containsMany;
	}

	public String getExpression() {
		return expression;
	}

	/**
	 * Add a ElGetValue element to the chain.
	 */
	public ElPropertyChainBuilder add(ElPropertyValue element) {
	    if (element == null){
	        throw new NullPointerException("element null in expression "+expression);
	    }
		chain.add(element);
		return this;
	}

	/**
	 * Build the immutable ElGetChain from the build information.
	 */
	public ElPropertyChain build() {
		return new ElPropertyChain(containsMany, embedded, expression, chain.toArray(new ElPropertyValue[chain.size()]));
	}

}
