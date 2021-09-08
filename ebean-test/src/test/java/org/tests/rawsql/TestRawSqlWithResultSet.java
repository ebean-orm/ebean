package org.tests.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.Transaction;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class TestRawSqlWithResultSet extends BaseTestCase {

  @Test
  public void test() throws SQLException {

    ResetBasicData.reset();


    PreparedStatement pstmt = null;

    // Transaction supplies our jdbc Connection
    Transaction txn = DB.beginTransaction();
    try {
      pstmt = txn.connection().prepareStatement("select id, name, billing_address_id from o_customer");

      // ResultSet will be closed by Ebean
      ResultSet resultSet = pstmt.executeQuery();

      RawSql rawSql = RawSqlBuilder.resultSet(resultSet, "id", "name", "billingAddress.id");

      List<Customer> list = DB.find(Customer.class)
        .setRawSql(rawSql)
        // also test a secondary query join
        .fetchQuery("billingAddress")
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
