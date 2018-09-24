package io.ebeaninternal.server.query;

import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.type.ScalarType;

import java.util.List;

/**
 * A property in the SQL Tree.
 * <p>
 * A BeanProperty or a dynamically created property based on formula.
 */
public interface STreeProperty {

  /**
   * Return the property name.
   */
  String getName();

  /**
   * Return the full property name (for error messages).
   */
  String getFullBeanName();

  /**
   * Return true if the property is the Id.
   */
  boolean isId();

  /**
   * Return true if the property is an embedded type.
   */
  boolean isEmbedded();

  /**
   * Return true if the property is an aggregation.
   */
  boolean isAggregation();

  /**
   * Return true if the property is a formula.
   */
  boolean isFormula();

  /**
   * Return the encryption key as a string value (when the property is encrypted).
   */
  String getEncryptKeyAsString();

  /**
   * Return the Expression language prefix (join path).
   */
  String getElPrefix();

  /**
   * Return the underlying scalar type for the property (for findSingleAttribute).
   */
  ScalarType<?> getScalarType();

  /**
   * For RawSql build the select chain.
   */
  void buildRawSqlSelectChain(String prefix, List<String> selectChain);

  /**
   * Load into the bean (from the DataReader/ResultSet).
   */
  void load(SqlBeanLoad sqlBeanLoad);

  /**
   * Ignore the property (moving the column index position without reading).
   */
  void loadIgnore(DbReadContext ctx);

  /**
   * Append to the select clause.
   */
  void appendSelect(DbSqlContext ctx, boolean subQuery);

  /**
   * Append to the from clause.
   */
  void appendFrom(DbSqlContext ctx, SqlJoinType joinType);

}
