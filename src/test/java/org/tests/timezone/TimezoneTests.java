package org.tests.timezone;

import io.ebean.Ebean;
import io.ebean.Transaction;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Must run this manually with various JVM timezones for insert and fetch.
 */
public class TimezoneTests {

  private final Timestamp nowTs = new Timestamp(1460000000000L);

  public TimezoneTests() {
  }

  @Ignore
  @Test
  public void rawJdbc() throws SQLException {

//    insert("Local");
//    insert("UTC");
//    insert("America/Los_Angeles");

//    System.out.println("Local");
//    fetch();

//    System.out.println("UTC");
//    setZone("UTC");
//    fetch();

    System.out.println("LA");
    setZone("America/Los_Angeles");
    fetch();
  }

  private void fetch() throws SQLException {
    try (
        Transaction transaction = Ebean.beginTransaction();
        Connection connection = transaction.getConnection();
        PreparedStatement statement = connection.prepareStatement("select * from tztest");
        ResultSet resultSet = statement.executeQuery()) {

      while (resultSet.next()) {
        System.out.println(" zone:" + resultSet.getString("zone"));
        System.out.println("   ts:" + tsof(resultSet.getTimestamp("ts")));
        System.out.println(" tstz:" + tsof(resultSet.getTimestamp("tstz")));
        System.out.println("  ts1:" + tsof(resultSet.getTimestamp("ts1", cal())));
        System.out.println("tstz1:" + tsof(resultSet.getTimestamp("tstz1", cal())));
      }
      System.out.println("");
    }
  }

  private String tsof(Timestamp timestamp) {
    return "" + timestamp.getTime() + ", " + timestamp.toString() + ", " + timestamp.toInstant();
  }

  private void setZone(String zone) {
    TimeZone.setDefault(TimeZone.getTimeZone(zone));
  }

  @SuppressWarnings("unused")
  private void insert(String zone) throws SQLException {

    String insert = "insert into tztest (zone, ts, tstz, ts1, tstz1) values (?,?,?,?,?)";

    if (!zone.equalsIgnoreCase("local")) {
      setZone(zone);
    }

    try (
        Transaction transaction = Ebean.beginTransaction();
        Connection connection = transaction.getConnection();
        PreparedStatement statement = connection.prepareStatement(insert)) {
      statement.setString(1, zone);
      statement.setTimestamp(2, nowTs);
      statement.setTimestamp(3, nowTs);
      statement.setTimestamp(4, nowTs, cal());
      statement.setTimestamp(5, nowTs, cal());
      statement.executeUpdate();

      transaction.commit();
    }
  }

  private Calendar cal() {

    Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    //Calendar instance = Calendar.getInstance(TimeZone.getDefault());
    return (Calendar) instance.clone();
  }
}
