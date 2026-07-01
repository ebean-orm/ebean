package io.ebeaninternal.server.query;

import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.deploy.TableJoin;

import java.util.List;
import java.util.Set;

/**
 * Represents the root node of the Sql Tree.
 */
final class SqlTreeNodeRoot extends SqlTreeNodeBean {

  private final TableJoin includeJoin;
  private final boolean sqlDistinct;
  private final String baseTable;

  /**
   * Specify for SqlSelect to include an Id property or not.
   */
  SqlTreeNodeRoot(STreeType desc, SqlTreeProperties props, List<SqlTreeNode> myList, boolean withId,
                  STreePropertyAssocMany many, SqlTreeCommon common, boolean sqlDistinct, String baseTable) {

    super(desc, props, myList, withId, many, common);
    this.includeJoin = common.includeJoin();
    this.sqlDistinct = sqlDistinct;
    this.baseTable = baseTable;
  }

  @Override
  public SqlTreeLoad createLoad() {
    return new SqlTreeLoadRoot(this);
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
      appendSelectId(ctx, idBinder.beanProperty());
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
    }
  }

  /**
   * For the root node there is no join type or on clause etc.
   */
  @Override
  public SqlJoinType appendFromBaseTable(DbSqlContext ctx, SqlJoinType joinType) {
    ctx.append(baseTable).append(" ").append(baseTableAlias).appendFromForUpdate();
    if (includeJoin != null) {
      // unique alias for intersection join
      includeJoin.addJoin(joinType, baseTableAlias, "int_", ctx);
    }
    return joinType;
  }

  @Override
  public void dependentTables(Set<String> tables) {
    tables.add(baseTable);
    for (SqlTreeNode child : children) {
      child.dependentTables(tables);
    }
  }
}
