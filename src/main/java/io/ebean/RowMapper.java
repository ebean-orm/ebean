package io.ebean;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Used with SqlQuery to map raw JDBC ResultSet to objects.
 * <p>
 * This provides a low level mapping option with direct use of JDBC ResultSet
 * with the option of having logic in the mapping. For example, only map some
 * columns depending on the values read from other columns.
 * </p>
 * <p>
 * For straight mapping into beans then DtoQuery would be the first choice as
 * it can automatically map the ResultSet into beans.
 * </p>
 *
 * <pre>{@code
 *
 *    //
 *    // A mapper from ResultSet into our CustomerDto bean
 *    //
 *    class CustomerMapper implements RowMapper<CustomerDto> {
 *
 *     @Override
 *     public CustomerDto map(ResultSet rset, int rowNum) throws SQLException {
 *
 *       long id = rset.getLong(1);
 *       String name = rset.getString(2);
 *       String status = rset.getString(3);
 *
 *       return new CustomerDto(id, name, status);
 *     }
 *   }
 *
 *
 *   //
 *   // Then use the mapper
 *   //
 *
 *   String sql = "select id, name, status from o_customer where name = ?";
 *
 *  CustomerDto rob = Ebean.createSqlQuery(sql)
 *    .setParameter(1, "Rob")
 *    .findOne(CUSTOMER_MAPPER);
 *
 *
 * }</pre>
 *
 * @param <T> The type the row data is mapped into.
 */
@FunctionalInterface
public interface RowMapper<T> {

  /**
   * Read the data from the ResultSet and map to the return type.
   *
   * @param resultSet The JDBC ResultSet positioned to the current row
   * @param rowNum    The number of the current row being mapped.
   */
  T map(ResultSet resultSet, int rowNum) throws SQLException;
}
