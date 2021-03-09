package io.ebeaninternal.server.query;

import io.ebean.Version;
import io.ebean.bean.EntityBean;
import io.ebean.core.type.ScalarDataReader;
import io.ebeaninternal.server.deploy.DbReadContext;

import java.sql.SQLException;

/**
 * The root level node of the SqlTree.
 */
interface SqlTreeRoot {

  /**
   * Load the bean from the DbReadContext.
   * <p>
   * At a high level this actually controls the reading of the data from the
   * jdbc resultSet and putting it into the bean etc.
   * </p>
   */
  EntityBean load(DbReadContext ctx) throws SQLException;

  /**
   * Load a version of a @History bean with effective dates.
   */
  <T> Version<T> loadVersion(DbReadContext ctx) throws SQLException;

  /**
   * Return a Scalar single attribute reader based on the first property.
   */
  ScalarDataReader<?> getSingleAttributeReader();

}
