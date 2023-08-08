package io.ebeaninternal.server.query;

import io.ebean.bean.EntityBean;
import io.ebean.core.type.ScalarDataReader;
import io.ebeaninternal.server.deploy.DbReadContext;

import java.sql.SQLException;

/**
 * Tree node that loads an entity bean type.
 */
interface SqlTreeLoad {

  /**
   * Load the appropriate information from the SqlSelectReader.
   * <p>
   * At a high level this actually controls the reading of the data from the
   * jdbc resultSet and putting it into the bean etc.
   * </p>
   */
  EntityBean load(DbReadContext ctx, EntityBean localBean, EntityBean contextBean) throws SQLException;

  /**
   * Return the reader for the single attribute query.
   */
  ScalarDataReader<?> singleAttributeReader();

}
