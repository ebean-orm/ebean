package io.ebeaninternal.server.query;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.DbReadContext;
import io.ebeaninternal.server.deploy.DbSqlContext;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

final class SqlTreeNodeManyRoot extends SqlTreeNodeBean {

  private final STreePropertyAssocMany manyProp;

  SqlTreeNodeManyRoot(String prefix, STreePropertyAssocMany prop, SqlTreeProperties props, List<SqlTreeNode> myList,
                      SpiQuery.TemporalMode temporalMode, boolean disableLazyLoad) {
    super(prefix, prop, props, myList, true, temporalMode, disableLazyLoad);
    this.manyProp = prop;
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

  @Override
  public EntityBean load(DbReadContext cquery, EntityBean parentBean, EntityBean contextParent) throws SQLException {
    // pass in null for parentBean because the localBean
    // that is built is added to a collection rather than
    // being set to the parentBean directly
    EntityBean detailBean = super.load(cquery, null, null);
    // initialise the collection and add detailBean if it is not null
    if (contextParent != null) {
      manyProp.addBeanToCollectionWithCreate(contextParent, detailBean, false);
    }
    return detailBean;
  }

  /**
   * Force outer join for everything after the many property.
   */
  @Override
  public void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {
    super.appendFrom(ctx, joinType.autoToOuter());
  }

  @Override
  public boolean hasMany() {
    return true;
  }
}
