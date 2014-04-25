package com.avaje.ebeaninternal.server.deploy.id;

import java.sql.SQLException;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;
import com.avaje.ebeaninternal.server.deploy.IntersectionRow;
import com.avaje.ebeaninternal.server.persist.dml.GenerateDmlRequest;
import com.avaje.ebeaninternal.server.persist.dmlbind.BindableRequest;

/**
 * Represents a imported property.
 */
public interface ImportedId {

	public void addFkeys(String name);
	
	/**
	 * Return true if this id is a simple single scalar value. False if it is a
	 * compound id (embedded or multiple).
	 */
	public boolean isScalar();

	/**
	 * Return the logical property name.
	 */
	public String getLogicalName();

	/**
	 * For scalar id return the related single db column.
	 * <p>
	 * This is essentially the imported foreign key column (where there is only
	 * one).
	 * </p>
	 */
	public String getDbColumn();

	/**
	 * Append the the SQL query statement.
	 */
	public void sqlAppend(DbSqlContext ctx);

	/**
	 * Append to the DML statement.
	 */
	public void dmlAppend(GenerateDmlRequest request);

	/**
	 * Append to the DML statement to the where clause.
	 */
	public void dmlWhere(GenerateDmlRequest request, EntityBean bean);
	
	/**
	 * Bind the value from the bean.
	 */
	public Object bind(BindableRequest request, EntityBean bean) throws SQLException;

	/**
	 * For inserting into ManyToMany intersection.
	 */
	public void buildImport(IntersectionRow row, EntityBean other);

	/**
	 * Used to derive a missing concatenated key from multiple imported keys.
	 */
	public BeanProperty findMatchImport(String matchDbColumn);
}