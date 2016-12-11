package org.tests.insert;

import io.ebean.BaseTestCase;
import org.junit.Test;

import java.sql.SQLException;

// CURRENTLY this test is failing with an upgrade of H2 so investigating that ...

public class TestSaveWithDaylightSavings extends BaseTestCase {

  @Test
  public void test() {

//    // For it to fail, the time has to match the time at which the daylight saving changes
//    // are applied in that time zone. Therefore specify it explicitly.
//
//    TimeZone defaultTimeZone = TimeZone.getDefault();
//    try {
//
//      TimeZone.setDefault(TimeZone.getTimeZone("EET"));
//
//      // Run the code and see how there is a 3600 second change
//      Timestamp daylightSavingDate = new Timestamp(1351382400000l);
//      // On a second run comment in the following date and see
//      // how there is a 0 second change
//      // daylightSavingDate = new Date(1361382400000l);
//
//      EBasic e = new EBasic();
//      e.setSomeDate(daylightSavingDate);
//
//      Ebean.save(e);
//      Assert.assertNotNull(e.getId());
//
//      // Reload the entity from database
//      EBasic e2 = Ebean.find(EBasic.class, e.getId());
//
//      long diffMillis = e2.getSomeDate().getTime() - e.getSomeDate().getTime();
//
//      System.out.println("The date I created " + daylightSavingDate);
//      System.out.println(" --- the date i put in   : " + e.getSomeDate());
//      System.out.println("          as millis      : " + e.getSomeDate().getTime());
//      System.out.println(" --- the date i get back : " + e2.getSomeDate());
//      System.out.println("          as millis      : " + e2.getSomeDate().getTime());
//      System.out.println("The difference is " + diffMillis / 1000 + " seconds");
//
//      assertEquals(0L, diffMillis);
//
//    } finally {
//      TimeZone.setDefault(defaultTimeZone);
//    }

  }

  @Test
  public void testDirect() throws SQLException {

//    // For it to fail, the time has to match the time at which the daylight saving changes
//    // are applied in that time zone. Therefore specify it explicitly.
//
//    EbeanServer server = Ebean.getServer(null);
//
//    Transaction transaction = server.createTransaction();
//    Connection connection = transaction.getConnection();
//
//    PreparedStatement pstmt = connection.prepareStatement("create table dls_test (id bigint auto_increment not null, myts timestamp)");
//    pstmt.execute();
//    pstmt.close();
//
//    TimeZone defaultTimeZone = TimeZone.getDefault();
//    try {
//
//      TimeZone.setDefault(TimeZone.getTimeZone("EET"));
//
//      // Run the code and see how there is a 3600 second change
//      Timestamp daylightSavingDate = new Timestamp(1351382400000l);
//      // On a second run comment in the following date and see
//      // how there is a 0 second change
//      // daylightSavingDate = new Date(1361382400000l);
//
//      pstmt = connection.prepareStatement("insert into dls_test (myts) values (?)");
//      pstmt.setTimestamp(1, daylightSavingDate);
//      assertEquals(1, pstmt.executeUpdate());
//      pstmt.close();
//
//      pstmt = connection.prepareStatement("select myts from dls_test ");
//      ResultSet rset = pstmt.executeQuery();
//      rset.next();
//      Timestamp timestampBack = rset.getTimestamp(1);
//      pstmt.close();
//      rset.close();
//
//
//      long diffMillis = daylightSavingDate.getTime() - timestampBack.getTime();
//
//      System.out.println(" --- the date i put in   : " + daylightSavingDate);
//      System.out.println("          as millis      : " + daylightSavingDate.getTime());
//      System.out.println(" --- the date i get back : " + timestampBack);
//      System.out.println("          as millis      : " + timestampBack.getTime());
//      System.out.println("The difference is " + diffMillis / 1000 + " seconds");
//
//      assertEquals(0L, diffMillis);
//
//    } finally {
//      TimeZone.setDefault(defaultTimeZone);
//    }

  }

}
