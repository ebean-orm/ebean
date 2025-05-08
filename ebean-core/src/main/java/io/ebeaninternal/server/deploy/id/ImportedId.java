package io.ebeaninternal.server.deploy.id;

import io.ebean.SqlUpdate;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.deploy.IntersectionBuilder;
import io.ebeaninternal.server.deploy.IntersectionRow;
import io.ebeaninternal.server.persist.dml.GenerateDmlRequest;
import io.ebeaninternal.server.persist.dmlbind.BindableRequest;

import java.sql.SQLException;

/**
 * Represents a imported property.
 */
public interface ImportedId {

  void addFkeys(String name);

  /**
   * Return true if this id is a simple single scalar value. False if it is a
   * compound id (embedded or multiple).
   */
  boolean isScalar();

  /**
   * For scalar id return the related single db column.
   * <p>
   * This is essentially the imported foreign key column (where there is only
   * one).
   * </p>
   */
  String getDbColumn();

  /**
   * Append the the SQL query statement.
   */
  void sqlAppend(DbSqlContext ctx);

  /**
   * Append to the DML statement.
   */
  void dmlAppend(GenerateDmlRequest request);

  /**
   * Append column and type for Insert (ClickHouse).
   */
  void dmlType(GenerateDmlRequest request);

  /**
   * Bind the value from the bean.
   */
  Object bind(BindableRequest request, EntityBean bean) throws SQLException;

  /**
   * Bind the imported Id value to the SqlUpdate.
   */
  int bind(int position, SqlUpdate update, EntityBean bean);

  /**
   * For inserting into ManyToMany intersection.
   */
  void buildImport(IntersectionRow row, EntityBean other);

  /**
   * Used to derive a missing concatenated key from multiple imported keys.
   */
  BeanProperty findMatchImport(String matchDbColumn);

  /**
   * Return the set importedId clause.
   */
  String importedIdClause();

  /**
   * Add DB columns to the intersection builder.
   */
  void buildImport(IntersectionBuilder row);

  /**
   * Bind values to the intersection SqlUpdate.
   */
  void bindImport(SqlUpdate sql, EntityBean other);
}
