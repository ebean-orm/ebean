package com.avaje.ebeaninternal.server.query;

import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;
import com.avaje.ebeaninternal.server.deploy.TableJoin;

/**
 * Represents the root node of the Sql Tree.
 */
public final class SqlTreeNodeRoot extends SqlTreeNodeBean {

	private final TableJoin includeJoin;
		
	/**
	 * Specify for SqlSelect to include an Id property or not.
	 */
	public SqlTreeNodeRoot(BeanDescriptor<?> desc, SqlTreeProperties props, List<SqlTreeNode> myList, boolean withId, TableJoin includeJoin, BeanPropertyAssocMany<?> many){
		super(null, null, desc, props, myList, withId, many);
		this.includeJoin = includeJoin;
	}

	public SqlTreeNodeRoot(BeanDescriptor<?> desc, SqlTreeProperties props, List<SqlTreeNode> myList, boolean withId) {
		super(null, null, desc, props, myList, withId, null);
		this.includeJoin = null;
	}
	
	@Override
	protected void postLoad(DbReadContext cquery, EntityBean loadedBean, Object id, Object lazyLoadParentId) {
		
		// set the current bean with id...
		cquery.setLoadedBean(loadedBean, id, lazyLoadParentId);
	}
	
  /**
   * For the root node there is no join type or on clause etc.
   */
  @Override
  public SqlJoinType appendFromBaseTable(DbSqlContext ctx, SqlJoinType joinType) {

    ctx.append(desc.getBaseTable());
    ctx.append(" ").append(ctx.getTableAlias(null));

    if (includeJoin != null) {
      String a1 = ctx.getTableAlias(null);
      String a2 = "int_"; // unique alias for intersection join
      includeJoin.addJoin(joinType, a1, a2, ctx);
    }

    return joinType;
  }
	
}
