package io.ebean;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Used with SqlQuery to process potentially large queries reading directly from the JDBC ResultSet.
 * <p>
 * This provides a low level option that reads directly from the JDBC ResultSet.
 * </p>
 *
 * <pre>{@code
 *
 *  String sql = "select id, name, status from o_customer order by name desc";
 *
 *  Ebean.createSqlQuery(sql)
 *    .findEachRow((resultSet, rowNum) -> {
 *
 *      // read directly from ResultSet
 *
 *      long id = resultSet.getLong(1);
 *      String name = resultSet.getString(2);
 *
 *      // do something interesting with the data
 *
 *    });
 *
 * }</pre>
 */
@FunctionalInterface
public interface RowConsumer {

  /**
   * Read the data from the ResultSet and process it.
   *
   * @param resultSet The JDBC ResultSet positioned to the current row
   * @param rowNum    The number of the current row being mapped.
   */
  void accept(ResultSet resultSet, int rowNum) throws SQLException;
}
