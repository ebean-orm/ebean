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
package com.avaje.ebeaninternal.server.deploy.parse;

import java.util.HashMap;

import com.avaje.ebeaninternal.server.deploy.TableJoin;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.meta.DeployTableJoin;

/**
 * Wraps information about a bean during deployment parsing.
 */
public class DeployBeanInfo<T> {

	/**
	 * Holds TableJoins for secondary table properties.
	 */
	private final HashMap<String,DeployTableJoin> tableJoinMap = new HashMap<String, DeployTableJoin>();

	private final DeployUtil util;

	private final DeployBeanDescriptor<T> descriptor;

	/**
	 * Create with a DeployUtil and BeanDescriptor.
	 */
	public DeployBeanInfo(DeployUtil util, DeployBeanDescriptor<T> descriptor) {
		this.util = util;
		this.descriptor = descriptor;
	}

	public String toString() {
		return ""+descriptor;
	}

	/**
	 * Return the BeanDescriptor currently being processed.
	 */
	public DeployBeanDescriptor<T> getDescriptor() {
		return descriptor;
	}

	/**
	 * Return the DeployUtil we are using.
	 */
	public DeployUtil getUtil() {
		return util;
	}
	
	/**
	 * Appropriate TableJoin for a property mapped to a secondary table.
	 */
	public DeployTableJoin getTableJoin(String tableName) {

		String key = tableName.toLowerCase();

		DeployTableJoin tableJoin = (DeployTableJoin) tableJoinMap.get(key);
		if (tableJoin == null) {
			tableJoin = new DeployTableJoin();
			tableJoin.setTable(tableName);
			tableJoin.setType(TableJoin.JOIN);
			descriptor.addTableJoin(tableJoin);

			tableJoinMap.put(key, tableJoin);
		}
		return tableJoin;
	}

	/**
	 * Set a the join alias for a assoc one property.
	 */
	public void setBeanJoinType(DeployBeanPropertyAssocOne<?> beanProp, boolean outerJoin) {

		String joinType = TableJoin.JOIN;
		if (outerJoin){// && util.isUseOneToOneOptional()) {
			joinType = TableJoin.LEFT_OUTER;
		}

		DeployTableJoin tableJoin = beanProp.getTableJoin();
		tableJoin.setType(joinType);
	}

}
