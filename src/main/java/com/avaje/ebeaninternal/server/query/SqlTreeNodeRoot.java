package com.avaje.ebeaninternal.server.query;

import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;
import com.avaje.ebeaninternal.server.deploy.TableJoin;

import java.util.List;

/**
 * Represents the root node of the Sql Tree.
 */
final class SqlTreeNodeRoot extends SqlTreeNodeBean {

  private final TableJoin includeJoin;

  /**
   * Specify for SqlSelect to include an Id property or not.
   */
  SqlTreeNodeRoot(BeanDescriptor<?> desc, SqlTreeProperties props, List<SqlTreeNode> myList, boolean withId,
                         TableJoin includeJoin, BeanPropertyAssocMany<?> many, SpiQuery.TemporalMode temporalMode, boolean disableLazyLoad) {

    super(desc, props, myList, withId, many, temporalMode, disableLazyLoad);
    this.includeJoin = includeJoin;
  }

  /**
   * Set AsOf support (at root level).
   */
  public void addAsOfTableAlias(SpiQuery<?> query) {
    if (desc.isHistorySupport()) {
      query.setAsOfBaseTable();
      query.incrementAsOfTableCount();
    }
    if (lazyLoadParent != null && lazyLoadParent.isManyToManyWithHistory()) {
      query.incrementAsOfTableCount();
    }
    for (int i = 0; i < children.length; i++) {
      children[i].addAsOfTableAlias(query);
    }
  }

  /**
   * For the root node there is no join type or on clause etc.
   */
  @Override
  public SqlJoinType appendFromBaseTable(DbSqlContext ctx, SqlJoinType joinType) {

    ctx.append(desc.getBaseTable(temporalMode));
    ctx.append(" ").append(baseTableAlias);

    if (includeJoin != null) {
      String a1 = baseTableAlias;
      String a2 = "int_"; // unique alias for intersection join
      includeJoin.addJoin(joinType, a1, a2, ctx);
    }

    return joinType;
  }

}
