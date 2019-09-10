package io.ebeaninternal.server.query;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.DbSqlContext;
import io.ebeaninternal.server.deploy.id.IdBinder;

public interface STreePropertyAssoc extends STreeProperty {

  /**
   * Return the extra where clause if set.
   */
  String getExtraWhere();

  /**
   * Return the type of the target (other side).
   */
  STreeType target();

  /**
   * Return the IdBinder of the underlying type.
   */
  IdBinder getIdBinder();

  /**
   * Add a Join with the given alias.
   */
  SqlJoinType addJoin(SqlJoinType joinType, String alias2, String alias, DbSqlContext ctx);

  /**
   * Add a Join with the given prefix (determining the alias).
   */
  SqlJoinType addJoin(SqlJoinType joinType, String prefix, DbSqlContext ctx);

  /**
   * Add a bean to the parent.
   */
  void setValue(EntityBean parentBean, Object contextBean);

}
