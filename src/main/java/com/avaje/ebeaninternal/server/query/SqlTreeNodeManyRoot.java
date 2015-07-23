package com.avaje.ebeaninternal.server.query;

import java.sql.SQLException;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;

public final class SqlTreeNodeManyRoot extends SqlTreeNodeBean {

  final BeanPropertyAssocMany<?> manyProp;

  public SqlTreeNodeManyRoot(String prefix, BeanPropertyAssocMany<?> prop, SqlTreeProperties props, List<SqlTreeNode> myList) {
    super(prefix, prop, prop.getTargetDescriptor(), props, myList, true, null, null);
    this.manyProp = prop;
  }

  @Override
  public EntityBean load(DbReadContext cquery, EntityBean parentBean, EntityBean contextParent) throws SQLException {
    // pass in null for parentBean because the localBean
    // that is built is added to a collection rather than
    // being set to the parentBean directly
    EntityBean detailBean = super.load(cquery, null, null);
    // initialise the collection and add detailBean if it is not null
    manyProp.addBeanToCollectionWithCreate(contextParent, detailBean);
    return detailBean;
  }

  /**
   * Force outer join for everything after the many property.
   */
  @Override
  public void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {
    super.appendFrom(ctx, joinType.autoToOuter());
  }

}
