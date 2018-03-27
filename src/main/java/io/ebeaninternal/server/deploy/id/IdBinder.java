package io.ebeaninternal.server.deploy.id;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiExpressionRequest;
import io.ebeaninternal.server.core.DefaultSqlUpdate;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.query.STreeProperty;
import io.ebeaninternal.server.type.DataBind;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Binds id values to prepared statements.
 */
public interface IdBinder {

  /**
   * Initialise the binder.
   */
  void initialise();

  /**
   * Return true if this is a compound key and must use expanded and or form.
   */
  boolean isIdInExpandedForm();

  /**
   * Write the Id value to binary DataOuput.
   */
  void writeData(DataOutput dataOutput, Object idValue) throws IOException;

  /**
   * Read the Id value from the binary DataInput.
   */
  Object readData(DataInput dataInput) throws IOException;

  /**
   * Return the name(s) of the Id property(s). Comma delimited if there is more
   * than one.
   * <p>
   * This can be used to include in a query.
   * </p>
   */
  String getIdProperty();

  /**
   * Return the Id BeanProperty.
   */
  STreeProperty getBeanProperty();

  /**
   * Find a BeanProperty that is mapped to the database column.
   */
  BeanProperty findBeanProperty(String dbColumnName);

  /**
   * Return false if the id is a simple scalar and false if it is embedded or
   * concatenated.
   */
  boolean isComplexId();

  /**
   * Return the default order by that may need to be used if the query includes
   * a many property.
   */
  String getDefaultOrderBy();

  String getOrderBy(String pathPrefix, boolean ascending);

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
  Object[] getBindValues(Object idValue);


  /**
   * For EmbeddedId convert the idValue into a simple map.
   * Otherwise the idValue is just returned as is.
   * <p>
   * This is used to provide a simple JSON serializable version of the id value.
   * </p>
   */
  Object getIdForJson(EntityBean idValue);

  /**
   * For EmbeddedId the value is assumed to be a Map and this is
   * takes the values from the map and builds an embedded id bean.
   * <p>
   * For other simple id's this just returns the value (no conversion required).
   * </p>
   * <p>
   * This is used to provide a simple JSON serializable version of the id value.
   * </p>
   */
  Object convertIdFromJson(Object value);

  /**
   * Return the id values for a given bean.
   */
  Object[] getIdValues(EntityBean bean);

  /**
   * Build a string of the logical expressions.
   * <p>
   * Typically used to build a id = ? string.
   * </p>
   */
  String getAssocOneIdExpr(String prefix, String operator);

  /**
   * Return the logical id in expression taking into account embedded id's.
   */
  String getAssocIdInExpr(String prefix);

  /**
   * Binds an id value to a prepared statement.
   */
  void bindId(DataBind dataBind, Object value) throws SQLException;

  /**
   * Bind the id value to a SqlUpdate statement.
   */
  void bindId(DefaultSqlUpdate sqlUpdate, Object value);

  /**
   * Binds multiple id value to an update.
   */
  void addIdInBindValues(DefaultSqlUpdate sqlUpdate, Collection<?> ids);

  /**
   * Binds multiple id value to a request.
   */
  void addIdInBindValues(SpiExpressionRequest request, Collection<?> ids);

  /**
   * Return the sql for binding the id using an IN clause.
   */
  String getBindIdInSql(String baseTableAlias);

  /**
   * Return the binding expression (like "?" or "(?,?)")for the Id.
   */
  String getIdInValueExpr(boolean not, int size);

  /**
   * Same as getIdInValueExpr but for delete by id.
   */
  String getIdInValueExprDelete(int size);

  void buildRawSqlSelectChain(String prefix, List<String> selectChain);

  /**
   * Read the id value from the result set and set it to the bean also returning
   * it.
   */
  Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException;

  /**
   * Ignore the appropriate number of scalar properties for this id.
   */
  void loadIgnore(DbReadContext ctx);

  /**
   * Read the id value from the result set and return it.
   */
  Object read(DbReadContext ctx) throws SQLException;

  /**
   * Append to the select clause.
   */
  void appendSelect(DbSqlContext ctx, boolean subQuery);

  /**
   * Return the sql for binding the id to. This includes table alias and columns
   * that make up the id.
   */
  String getBindIdSql(String baseTableAlias);

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
  Object convertSetId(Object idValue, EntityBean bean);

  /**
   * Cast or convert the Id value if necessary.
   */
  Object convertId(Object idValue);
}
