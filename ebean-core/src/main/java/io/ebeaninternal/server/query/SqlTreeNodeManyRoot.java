package io.ebeaninternal.server.query;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;

import java.sql.SQLException;
import java.util.List;

final class SqlTreeNodeManyRoot extends SqlTreeNodeBean {

  private final STreePropertyAssocMany manyProp;

  SqlTreeNodeManyRoot(String prefix, STreePropertyAssocMany prop, SqlTreeProperties props, List<SqlTreeNode> myList,
                      SpiQuery.TemporalMode temporalMode, boolean disableLazyLoad) {
    super(prefix, prop, props, myList, true, temporalMode, disableLazyLoad);
    this.manyProp = prop;
  }

  @Override
  public boolean hasMany() {
    return true;
  }

  @Override
  public EntityBean load(DbReadContext cquery, EntityBean parentBean, EntityBean contextParent) throws SQLException {
    // pass in null for parentBean because added to a collection rather than set to the parentBean
    SqlTreeNodeBean.Load load = createLoad(cquery, null);
    EntityBean detailBean = load.perform();
    if (contextParent != null) {
      // Add to the collection and initialise collection if needed
      // A null detailBean may initialise an empty collection
      // Check for bean existing in collection based on load.isContextBean()
      manyProp.addBeanToCollectionWithCreate(contextParent, detailBean, load.isContextBean());
    }
    return detailBean;
  }

  /**
   * Append the property columns to the buffer.
   */
  @Override
  public void appendDistinctOn(DbSqlContext ctx, boolean subQuery) {
    ctx.pushTableAlias(prefix);
    appendSelectId(ctx, idBinder.getBeanProperty());
    ctx.popTableAlias();
  }

  /**
   * append extraWhere to the join.
   */
  @Override
  protected SqlJoinType appendFromAsJoin(DbSqlContext ctx, SqlJoinType joinType) {
    SqlJoinType join = super.appendFromAsJoin(ctx, joinType);
    super.appendExtraWhere(ctx);
    return join;
  }

  @Override
  protected void appendExtraWhere(DbSqlContext ctx) {
    // extraWhere is already appended to the tableJoin
  }

  /**
   * Force outer join for everything after the many property.
   */
  @Override
  public void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {
    super.appendFrom(ctx, joinType.autoToOuter());
  }
}
