package com.avaje.ebeaninternal.server.deploy.id;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;

import com.avaje.ebeaninternal.api.SpiExpressionRequest;
import com.avaje.ebeaninternal.server.core.DefaultSqlUpdate;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;
import com.avaje.ebeaninternal.server.type.DataBind;

/**
 * Binds id values to prepared statements.
 */
public interface IdBinder {

  /**
   * Initialise the binder.
   */
  public void initialise();

  /**
   * Return true if this is a compound key and must use expanded and or form.
   */
  public boolean isIdInExpandedForm();

  /**
   * Write the Id value to binary DataOuput.
   */
  public void writeData(DataOutput dataOutput, Object idValue) throws IOException;

  /**
   * Read the Id value from the binary DataInput.
   */
  public Object readData(DataInput dataInput) throws IOException;

  /**
   * Return the name(s) of the Id property(s). Comma delimited if there is more
   * than one.
   * <p>
   * This can be used to include in a query.
   * </p>
   */
  public String getIdProperty();

  /**
   * Return the Id BeanProperty.
   */
  public BeanProperty getBeanProperty();

  /**
   * Find a BeanProperty that is mapped to the database column.
   */
  public BeanProperty findBeanProperty(String dbColumnName);

  /**
   * Return the number of scalar properties for this id.
   */
  public int getPropertyCount();

  /**
   * Return false if the id is a simple scalar and false if it is embedded or
   * concatenated.
   */
  public boolean isComplexId();

  /**
   * Return the default order by that may need to be used if the query includes
   * a many property.
   */
  public String getDefaultOrderBy();

  public String getOrderBy(String pathPrefix, boolean ascending);

  /**
   * Return the values as an array of scalar bindable values.
   * <p>
   * For concatenated keys that use an Embedded bean or multiple id properties
   * this determines the field values are returns them as an Object array.
   * </p>
   * <p>
   * Added primarily for Query.addWhere().add(Expr.idEq()) support.
   * </p>
   */
  public Object[] getBindValues(Object idValue);

  /**
   * Return the id values for a given bean.
   */
  public Object[] getIdValues(EntityBean bean);

  /**
   * Build a string of the logical expressions.
   * <p>
   * Typically used to build a id = ? string.
   * </p>
   */
  public String getAssocOneIdExpr(String prefix, String operator);

  /**
   * Return the logical id in expression taking into account embedded id's.
   */
  public String getAssocIdInExpr(String prefix);

  /**
   * Binds an id value to a prepared statement.
   */
  public void bindId(DataBind dataBind, Object value) throws SQLException;

  /**
   * Bind the id value to a SqlUpdate statement.
   */
  public void bindId(DefaultSqlUpdate sqlUpdate, Object value);

  public void addIdInBindValue(SpiExpressionRequest request, Object value);

  /**
   * Return the sql for binding the id using an IN clause.
   */
  public String getBindIdInSql(String baseTableAlias);

  /**
   * Return the binding expression (like "?" or "(?,?)")for the Id.
   */
  public String getIdInValueExpr(int size);

  /**
   * Same as getIdInValueExpr but for delete by id.
   */
  public String getIdInValueExprDelete(int size);

  public void buildSelectExpressionChain(String prefix, List<String> selectChain);

  /**
   * Read the id value from the result set and set it to the bean also returning
   * it.
   */
  public Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException;

  /**
   * Ignore the appropriate number of scalar properties for this id.
   */
  public void loadIgnore(DbReadContext ctx);

  /**
   * Read the id value from the result set and return it.
   */
  public Object read(DbReadContext ctx) throws SQLException;

  /**
   * Append to the select clause.
   */
  public void appendSelect(DbSqlContext ctx, boolean subQuery);

  /**
   * Return the sql for binding the id to. This includes table alias and columns
   * that make up the id.
   */
  public String getBindIdSql(String baseTableAlias);

  /**
   * Cast or convert the Id value if necessary and optionally set it.
   * <p>
   * The Id value is not assumed to be the correct type so it is converted to
   * the correct type. Typically this is because we could get a Integer, Long or
   * BigDecimal depending on the JDBC driver and situation.
   * </p>
   * <p>
   * If the bean is not null, then the value is set to the bean.
   * </p>
   */
  public Object convertSetId(Object idValue, EntityBean bean);

}
