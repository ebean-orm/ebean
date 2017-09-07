package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.Transaction;

import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement;

import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestInEmpty extends BaseTestCase {

  private static final int MAX_PARAMS = 100;
  
  @Test
  @Ignore
  public void test_in_empty() {

    Query<Order> query = Ebean.find(Order.class).where().in("id", new Object[0]).gt("id", 0)
      .query();

    List<Order> list = query.findList();
    assertThat(query.getGeneratedSql()).contains("1=0");
    assertEquals(0, list.size());
  }

  @Test
  @Ignore
  public void test_notIn_empty() {

    Query<Order> query = Ebean.find(Order.class).where().notIn("id", new Object[0]).gt("id", 0)
      .query();

    query.findList();
    assertThat(query.getGeneratedSql()).contains("1=1");
  }

  
  @Test
  public void test_in_many() {

    Object[] values = new Object[MAX_PARAMS];
    for (int i = 0; i < values.length; i++) {
      values[i] = i;
    }
    Query<Order> query = Ebean.find(Order.class).where().in("id", values).gt("id", 0)
      .query();

    List<Order> list = query.findList();
    assertThat(query.getGeneratedSql()).contains("1=0");
    assertEquals(0, list.size());
  }
//
//  @Test
//  public void test_notIn_many() {
//    Object[] values = new Object[MAX_PARAMS];
//    for (int i = 0; i < values.length; i++) {
//      values[i] = i;
//    }
//    Query<Order> query = Ebean.find(Order.class).where().notIn("id", values).gt("id", 0)
//      .query();
//
//    query.findList();
//    assertThat(query.getGeneratedSql()).contains("1=1");
//  }
  @Test
  @Ignore
  public void test_notIn_many() throws SQLServerException {

    
    
    Object[] values = new Object[MAX_PARAMS];
    for (int i = 0; i < values.length; i++) {
      values[i] = i;
    }
    
    SQLServerDataTable arrayType = new SQLServerDataTable();
    arrayType.addColumnMetadata("item", java.sql.Types.INTEGER);
    for (Object value: values) {
      arrayType.addRow(value);
    }
    //p
      
    //pstmt.setStructured(1, "dbo.IntegerTable", accounts);
    
    Query<Order> query = Ebean.find(Order.class).where().notIn("id", values)
        .query();

    query.findList();
    assertThat(query.getGeneratedSql()).contains("1=1");
  }
  @Test
  public void testRaw() throws SQLException {
    ResetBasicData.reset();
    String sql = "select from o_order where id in (?)";
    Transaction txn = Ebean.beginTransaction();
    try {
      Connection conn = txn.getConnection();
      
      
      //java.sql.Timestamp value = java.sql.Timestamp.valueOf("2007-09-23 10:10:10.123");

      SQLServerDataTable tvp = new SQLServerDataTable();
      tvp.setTvpName("table");
      tvp.addColumnMetadata("item", java.sql.Types.INTEGER);
      tvp.addRow(1);
      tvp.addRow(3);
      PreparedStatement pstmt = conn.prepareStatement("select * from o_product where id in (select * from ?) ;");
      //PreparedStatement pstmt = conn.prepareStatement("select count(*) from o_product where id in (select * from ?)");
      SQLServerPreparedStatement spstmt = pstmt.unwrap(SQLServerPreparedStatement.class);
      spstmt.setStructured(1, "dbo.IntegerTable", tvp);

      System.out.println(1);
      ResultSet rset = spstmt.executeQuery();
      System.out.println(2);
      while (rset.next()) {
        System.out.println(rset.getString(1)+"\t"+rset.getString(2)+"\t"+rset.getString(3));
      }
//      PreparedStatement pstmt = conn.prepareStatement("select * from ? ;");
//
//
//      SQLServerDataTable tvp = new SQLServerDataTable();
//      tvp.addColumnMetadata("c1", java.sql.Types.INTEGER);
//      tvp.addRow(47);
//      tvp.addRow(11);
//
//      spstmt.setStructured(1, "dbo.IntegerTable", tvp);
//      ResultSet rset = pstmt.executeQuery();
//      while (rset.next()) {
//        System.out.println(rset.getString(1));
//      }
      txn.commit();
    } finally {
      txn.end();
    }
  }
}
