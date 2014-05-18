package com.avaje.ebeaninternal.server.query;

import java.sql.SQLException;
import java.util.List;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.DbReadContext;
import com.avaje.ebeaninternal.server.deploy.DbSqlContext;

public interface SqlTreeNode {

  public static final String PERIOD = ".";

  public static final String COMMA = ", ";

  public static final int NORMAL = 0;
  public static final int SHARED = 1;
  public static final int READONLY = 2;

  public void buildSelectExpressionChain(List<String> selectChain);

  /**
   * Append the required column information to the SELECT part of the sql
   * statement.
   */
  public void appendSelect(DbSqlContext ctx, boolean subQuery);

  /**
   * Append to the FROM part of the sql.
   */
  public void appendFrom(DbSqlContext ctx, SqlJoinType joinType);

  /**
   * Append any where predicates for inheritance.
   */
  public void appendWhere(DbSqlContext ctx);

  /**
   * Load the appropriate information from the SqlSelectReader.
   * <p>
   * At a high level this actually controls the reading of the data from the
   * jdbc resultSet and putting it into the bean etc.
   * </p>
   * 
   */
  public void load(DbReadContext ctx, EntityBean parentBean) throws SQLException;

}
