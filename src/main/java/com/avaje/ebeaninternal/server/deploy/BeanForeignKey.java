package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebeaninternal.server.core.InternString;

/**
 * Represents a database foreign key which can map to an object relationship.
 */
public class BeanForeignKey {

	private final String dbColumn;

	private final int dbType;

	/**
	 * Construct the BeanForeignKey.
	 */
	public BeanForeignKey(String dbColumn, int dbType) {
		this.dbColumn = InternString.intern(dbColumn);
		this.dbType = dbType;
	}

	/**
	 * Return the database column.
	 */
	public String getDbColumn() {
		return dbColumn;
	}

	/**
	 * Return the JDBC datatype of the database column.
	 */
	public int getDbType() {
		return dbType;
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof BeanForeignKey) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	public int hashCode() {
		int hc = getClass().hashCode();
		hc = hc * 31 + (dbColumn != null ? dbColumn.hashCode() : 0);
		return hc;
	}

	public String toString() {
		return dbColumn;
	}

}
