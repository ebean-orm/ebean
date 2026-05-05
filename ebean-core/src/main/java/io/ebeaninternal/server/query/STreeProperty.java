package io.ebeaninternal.server.query;

import io.ebean.core.type.ScalarDataReader;
import io.ebean.core.type.ScalarType;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;

import java.util.List;
import java.util.Set;

/**
 * A property in the SQL Tree.
 * <p>
 * A BeanProperty or a dynamically created property based on formula.
 */
public interface STreeProperty extends ScalarDataReader<Object> {

  /**
   * Return the property name.
   */
  String name();

  /**
   * Return the full property name (for error messages).
   */
  String fullName();

  /**
   * Return true if the property is the Id.
   */
  boolean isId();

  /**
   * Returns true, if this is a lob property from db-perspective.
   */
  boolean isLobForPlatform();

  /**
   * Return true if the property is an embedded type.
   */
  boolean isEmbedded();

  /**
   * Return true if the property is an aggregation.
   */
  boolean isAggregation();

  /**
   * Return true if the property is an aggregation on a ManyToOne.
   */
  default boolean isAggregationManyToOne() {
    return false;
  }

  /**
   * Return true if the property is a formula.
   */
  boolean isFormula();

  /**
   * Return the encryption key as a string value (when the property is encrypted).
   */
  String encryptKeyAsString();

  /**
   * Return the Expression language prefix (join path).
   */
  String elPrefix();

  /**
   * Return the underlying scalar type for the property (for findSingleAttribute).
   */
  ScalarType<?> scalarType();

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
   * Append to group by.
   */
  default void appendGroupBy(DbSqlContext ctx, boolean subQuery) {
    appendSelect(ctx, subQuery);
  }

  /**
   * Append to the from clause.
   */
  void appendFrom(DbSqlContext ctx, SqlJoinType joinType, String manyWhere);

  default void extraIncludes(Set<String> predicateIncludes) {
    // do nothing
  }
}
