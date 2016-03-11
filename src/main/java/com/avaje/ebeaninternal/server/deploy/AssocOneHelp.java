package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.query.SqlJoinType;

import java.sql.SQLException;

/**
 * Local interface to handle Embedded, Reference and Reference Exported
 * cases.
 */
abstract class AssocOneHelp {

  /**
   * Effectively skip reading (the jdbc resultSet as already in the persistence context etc).
   */
  abstract void loadIgnore(DbReadContext ctx);

  /**
   * Read and return the bean.
   */
  abstract Object read(DbReadContext ctx) throws SQLException;

  /**
   * Read setting values into the bean.
   */
  abstract Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException;

  /**
   * Append to the select clause.
   */
  abstract void appendSelect(DbSqlContext ctx, boolean subQuery);

  /**
   * Append to the from clause.
   */
  abstract void appendFrom(DbSqlContext ctx, SqlJoinType joinType);

}
