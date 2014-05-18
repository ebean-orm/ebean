package com.avaje.ebeaninternal.server.query;

import java.sql.SQLException;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;

public final class SqlTreeNodeManyRoot extends SqlTreeNodeBean {

  public SqlTreeNodeManyRoot(String prefix, BeanPropertyAssocMany<?> prop, SqlTreeProperties props, List<SqlTreeNode> myList) {
    super(prefix, prop, prop.getTargetDescriptor(), props, myList, true, null);
  }

  @Override
  protected void postLoad(DbReadContext cquery, EntityBean loadedBean, Object id, Object lazyLoadParentId) {

    // put the localBean into the manyValue so that it
    // is added to the collection/map
    cquery.setLoadedManyBean(loadedBean);
  }

  @Override
  public void load(DbReadContext cquery, EntityBean parentBean) throws SQLException {
    // pass in null for parentBean because the localBean
    // that is built is added to a collection rather than
    // being set to the parentBean directly
    super.load(cquery, null);
  }

  /**
   * Force outer join for everything after the many property.
   */
  @Override
  public void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {
    super.appendFrom(ctx, joinType.autoToOuter());
  }

}
