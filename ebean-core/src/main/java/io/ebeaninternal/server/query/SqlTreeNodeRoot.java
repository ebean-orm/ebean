package io.ebeaninternal.server.query;

import io.ebean.Version;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.deploy.TableJoin;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * Represents the root node of the Sql Tree.
 */
final class SqlTreeNodeRoot extends SqlTreeNodeBean implements SqlTreeRoot {

  private final TableJoin includeJoin;

  private final boolean sqlDistinct;

  private final String baseTable;

  /**
   * Specify for SqlSelect to include an Id property or not.
   */
  SqlTreeNodeRoot(STreeType desc, SqlTreeProperties props, List<SqlTreeNode> myList, boolean withId, TableJoin includeJoin,
                  STreePropertyAssocMany many, SpiQuery.TemporalMode temporalMode, boolean disableLazyLoad, boolean sqlDistinct, String baseTable) {

    super(desc, props, myList, withId, many, temporalMode, disableLazyLoad);
    this.includeJoin = includeJoin;
    this.sqlDistinct = sqlDistinct;
    this.baseTable = baseTable;
  }

  @Override
  protected boolean isRoot() {
    return true;
  }

  @Override
  public EntityBean load(DbReadContext ctx) throws SQLException {
    return load(ctx, null, null);
  }

  /**
   * Read the version bean.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> Version<T> loadVersion(DbReadContext ctx) throws SQLException {
    // read the sys period lower and upper bounds
    // these are always the first 2 columns in the resultSet
    Timestamp start = ctx.getDataReader().getTimestamp();
    Timestamp end = ctx.getDataReader().getTimestamp();
    T bean = (T) load(ctx, null, null);
    return new Version<>(bean, start, end);
  }

  @Override
  public boolean isSqlDistinct() {
    return sqlDistinct;
  }

  /**
   * Append the property columns to the buffer.
   */
  @Override
  public void appendDistinctOn(DbSqlContext ctx, boolean subQuery) {
    if (readId) {
      ctx.pushTableAlias(prefix);
      appendSelectId(ctx, idBinder.getBeanProperty());
      ctx.popTableAlias();
      super.appendDistinctOn(ctx, subQuery);
    }
  }

  /**
   * Set AsOf support (at root level).
   */
  @Override
  public void addAsOfTableAlias(SpiQuery<?> query) {
    if (desc.isHistorySupport()) {
      query.setAsOfBaseTable();
      query.incrementAsOfTableCount();
    }
    if (lazyLoadParent != null && lazyLoadParent.isManyToManyWithHistory()) {
      query.incrementAsOfTableCount();
    }
    for (SqlTreeNode aChildren : children) {
      aChildren.addAsOfTableAlias(query);
    }
  }

  /**
   * For the root node there is no join type or on clause etc.
   */
  @Override
  public SqlJoinType appendFromBaseTable(DbSqlContext ctx, SqlJoinType joinType) {
    ctx.append(baseTable);
    ctx.append(" ").append(baseTableAlias);
    ctx.appendFromForUpdate();
    if (includeJoin != null) {
      String a1 = baseTableAlias;
      String a2 = "int_"; // unique alias for intersection join
      includeJoin.addJoin(joinType, a1, a2, ctx);
    }
    return joinType;
  }

  @Override
  public void dependentTables(Set<String> tables) {
    for (SqlTreeNode child : children) {
      child.dependentTables(tables);
    }
  }
}
