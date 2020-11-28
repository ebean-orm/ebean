package org.tests.timezone;

import io.ebean.Ebean;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;
import java.util.TimeZone;
//import java.util.TimeZone;


// Postgres
// manually create table
// create table tz_bean (id serial, mode varchar(40), ts timestamp, tstz timestamp with time zone);

// MySql
// manually create table (note there is no timestamp with time zone type)
// create table tz_bean (id serial, mode varchar(40), ts timestamp, tstz timestamp);

// Oracle
// create table tz_bean (id integer, run_mode varchar(40), ts timestamp, tstz timestamp);

/**
 * Must run this test manually with various time zones for insert and fetch.
 */
public class TzBeanTests {

  private final Timestamp nowTs = new Timestamp(1460000000000L);

  public TzBeanTests() {

  }

  /**
   * Run this 5 times with different modes.
   */
  @Ignore
  @Test
  public void insert_one_at_a_time() {

    //String mode = "noCal + local tz";

//    String mode = "noCal + LA";
//    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));

//    String mode = "noCal + UTC";
//    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

//    System.setProperty("ebean.dataTimeZone", "UTC");
//    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
//    String mode = "UTC + LA";

    System.setProperty("ebean.dataTimeZone", "UTC");
    String mode = "UTC + NZST";

    TzBean bean = new TzBean();
    bean.setMode(mode);
    bean.setTs(nowTs);
    bean.setTstz(nowTs);

    Ebean.save(bean);
  }

  /**
   * In separate JVM/execution fetch the beans with various timezones etc.
   */
  @Ignore
  @Test
  public void fetch_beans() {


    // set the jvm timezone
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));

    // set the Calendar time zone to use in JDBC calls
    System.setProperty("ebean.dataTimeZone", "UTC");

    List<TzBean> list = Ebean.find(TzBean.class)
      .findList();

    for (TzBean bean : list) {
      System.out.println(" mode:" + bean.getMode());
      System.out.println("   ts:" + tsof(bean.getTs()));
      System.out.println(" tstz:" + tsof(bean.getTstz()));
    }
  }

  private String tsof(Timestamp timestamp) {
    return "" + timestamp.getTime() + ", " + timestamp.toString() + ", " + timestamp.toInstant();
  }
}
