package com.avaje.ebean.config;

import java.sql.PreparedStatement;

/**
 * Unwrap the PreparedStatement to get the specific underlying implementation.
 * <p>
 * This is used to handle specific JDBC driver issues. Typically this means
 * getting the OraclePreparedStatement to handle Oracle specific issues etc.
 * </p>
 * 
 * @author rbygrave
 */
public interface PstmtDelegate {

  /**
   * Unwrap the PreparedStatement to get the specific underlying implementation.
   * 
   * @param pstmt
   *          the PreparedStatement coming out of the connection pool
   * @return the underlying PreparedStatement
   */
  public PreparedStatement unwrap(PreparedStatement pstmt);
}
