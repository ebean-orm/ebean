package com.avaje.ebeaninternal.server.deploy.parse;

import java.util.HashMap;

import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.meta.DeployTableJoin;
import com.avaje.ebeaninternal.server.query.SqlJoinType;

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
			tableJoin.setType(SqlJoinType.INNER);
			descriptor.addTableJoin(tableJoin);

			tableJoinMap.put(key, tableJoin);
		}
		return tableJoin;
	}

	/**
	 * Set a the join alias for a assoc one property.
	 */
	public void setBeanJoinType(DeployBeanPropertyAssocOne<?> beanProp, boolean outerJoin) {

		DeployTableJoin tableJoin = beanProp.getTableJoin();
		tableJoin.setType(outerJoin ? SqlJoinType.OUTER : SqlJoinType.INNER);
	}

}
