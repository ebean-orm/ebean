package com.avaje.tests.rawsql;

import com.avaje.ebean.*;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class TestRawSqlWithResultSet extends BaseTestCase {

  @Test
  public void test() throws SQLException {

    ResetBasicData.reset();

    // Transaction supplies our jdbc Connection
    Transaction txn = Ebean.beginTransaction();

    PreparedStatement pstmt = null;

    try {
      pstmt = txn.getConnection().prepareStatement("select id, name, billing_address_id from o_customer");

      // ResultSet will be closed by Ebean
      ResultSet resultSet = pstmt.executeQuery();

      RawSql rawSql = new RawSql(resultSet, "id", "name", "billingAddress.id");

      List<Customer> list = Ebean.find(Customer.class)
        .setRawSql(rawSql)
        // also test a secondary query join
        .fetch("billingAddress", new FetchConfig().query())
        .findList();

      for (Customer customer : list) {
        customer.getId();
        customer.getName();
        customer.getBillingAddress();
      }

    } finally {
      close(pstmt);
      txn.end();
    }

  }

  private static void close(Statement stmt) {

    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

}
