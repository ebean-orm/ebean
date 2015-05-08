package com.avaje.ebeaninternal.server.jdbc;

import java.sql.PreparedStatement;

import com.avaje.ebean.config.PstmtDelegate;
import com.avaje.ebeaninternal.server.lib.sql.ExtendedPreparedStatement;

/**
 * Implementation of PstmtDelegate from Ebean's own DataSource.
 */
public class StandardPstmtDelegate implements PstmtDelegate {

  /**
   * Unwrap the PreparedStatement from Ebean's DataSource implementation.
   */
  public PreparedStatement unwrap(PreparedStatement pstmt) {

    return ((ExtendedPreparedStatement) pstmt).getDelegate();
  }
}
