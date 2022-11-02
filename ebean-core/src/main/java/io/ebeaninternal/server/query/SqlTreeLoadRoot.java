package io.ebeaninternal.server.query;

import io.ebean.Version;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.DbReadContext;

import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Represents the root node of the Sql Tree.
 */
final class SqlTreeLoadRoot extends SqlTreeLoadBean implements SqlTreeRoot {

  SqlTreeLoadRoot(SqlTreeNodeBean node) {
    super(node);
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
    Timestamp start = ctx.dataReader().getTimestamp();
    Timestamp end = ctx.dataReader().getTimestamp();
    T bean = (T) load(ctx, null, null);
    return new Version<>(bean, start, end);
  }

}
