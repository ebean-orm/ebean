package org.tests.timezone;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.xtest.BaseTestCase;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TimeTypesTest extends BaseTestCase {

  protected Database dbGmt;
  protected Database dbEurope;
  protected Database dbPST;

  @BeforeAll
  public void startTest() {
    // both DBs are connected to the same datasource
    dbGmt = createServer("GMT");
    dbEurope = createServer("Europe/Berlin");
    dbPST = createServer("PST");
  }

  @AfterAll
  public void shutdown() {
    if (dbGmt != null) {
      dbGmt.shutdown();
    }
    if (dbEurope != null) {
      dbEurope.shutdown();
    }
    if (dbPST != null) {
      dbPST.shutdown();
    }
  }

  @BeforeEach
  public void clearDb() {
    dbGmt.find(MTimeTest.class).delete();
  }

  /**
   * Test for instant.
   */
  @Test
  public void testInstant() {

    dbGmt.sqlUpdate("insert into mtime_test (id, instant) values (1, '2021-11-21 05:15:15')").execute();

    Instant inst = Instant.parse("2021-11-21T05:15:15Z");
    testTimeType(MTimeTest.class, "instant", MTimeTest::getInstant, inst);

    assertThat(dbEurope.find(MTimeTest.class).findOne().getInstant()) // we get 04:15:15/GMT here, as this is equal to 05:15:15/Europe
      .isEqualTo(inst.minusSeconds(3600));

    assertThat(dbPST.find(MTimeTest.class).findOne().getInstant())
      .isEqualTo(inst.plusSeconds(8 * 3600));
  }

  @Test
  public void testLocalDate() {
    dbGmt.sqlUpdate("insert into mtime_test (id, local_date) values (1, '2021-11-21')").execute();

    LocalDate ld = LocalDate.parse("2021-11-21");
    testTimeType(MTimeTest.class, "local_date", MTimeTest::getLocalDate, ld);

    assertThat(dbEurope.find(MTimeTest.class).findOne().getLocalDate()).isEqualTo(ld);

    assertThat(dbPST.find(MTimeTest.class).findOne().getLocalDate()).isEqualTo(ld);
  }

  @Test
  public void testLocalDateTime() {
    dbGmt.sqlUpdate("insert into mtime_test (id, local_date_time) values (1, '2021-11-21 05:15:15')").execute();

    LocalDateTime ldt = LocalDateTime.parse("2021-11-21T05:15:15");

    // Expected:
    //  testTimeType(MTimeTest.class, "local_date_time", MTimeTest::getLocalDateTime, ldt);
    //  assertThat(dbEurope.find(MTimeTest.class).findOne().getLocalDateTime()).isEqualTo(ldt);
    //  assertThat(dbPST.find(MTimeTest.class).findOne().getLocalDateTime()).isEqualTo(ldt);

    // Actual (if machine runs in Europe/Berlin):
    // This is something I would not expect, or at least has to be discussed
    // A LocalDateTime mapped to a DB column should IMHO be never adjusted
    // Arguments: A LocalDateTime = LocalDate + LocalTime. Having these types in separate columns, they are also not adjusted
    assertThat(dbGmt.find(MTimeTest.class).findOne().getLocalDateTime()).isEqualTo(ldt.plusSeconds(3600));
    assertThat(dbEurope.find(MTimeTest.class).findOne().getLocalDateTime()).isEqualTo(ldt);
    assertThat(dbPST.find(MTimeTest.class).findOne().getLocalDateTime()).isEqualTo(ldt.plusSeconds(9 * 3600));

  }

  @Test
  public void testLocalTime() {
    dbGmt.sqlUpdate("insert into mtime_test (id, local_time) values (1, '05:15:15')").execute();

    LocalTime lt = LocalTime.of(5, 15, 15);

    testTimeType(MTimeTest.class, "local_time", MTimeTest::getLocalTime, lt);

    assertThat(dbEurope.find(MTimeTest.class).findOne().getLocalTime()).isEqualTo(lt);

    assertThat(dbPST.find(MTimeTest.class).findOne().getLocalTime()).isEqualTo(lt);

  }

  @Test
  public void testTimestamp() {
    dbGmt.sqlUpdate("insert into mtime_test (id, timestamp) values (1, '2021-11-21 05:15:15')").execute();

    Timestamp ts = Timestamp.from(Instant.parse("2021-11-21T05:15:15Z"));

    testTimeType(MTimeTest.class, "timestamp", MTimeTest::getTimestamp, ts);


    assertThat(dbEurope.find(MTimeTest.class).findOne().getTimestamp()).isEqualTo(new Timestamp(ts.getTime() - 3600_000));

    assertThat(dbPST.find(MTimeTest.class).findOne().getTimestamp()).isEqualTo(new Timestamp(ts.getTime() + 8 * 3600_000));
  }

  /**
   * This checks for each datatype, if it can be read properly from DB.
   * <ol>
   *   <li>Check, if the read value is the same as <code>ref</code></li>
   *   <li>Perform a findcount with <code>eq(field, ref)</code></li>
   *   <li>Perform a findOnw with <code>eq(field, ref)</code></li>
   *   <li>Perform a findOnw with <code>in(field, ref)</code></li>
   * </ol>
   */
  <M, T> void testTimeType(Class<M> modelClass, String field, Function<M, T> getter, T ref) {
    SoftAssertions softly = new SoftAssertions();
    // Check 1: There should be ONE model in the DB
    M dbModel = dbGmt.find(modelClass).findOne();
    softly.assertThat(getter.apply(dbModel).toString())
      .as("test read value")
      .isEqualTo(ref.toString());

    int count = dbGmt.find(modelClass).where().eq(field, ref).findCount();
    softly.assertThat(count)
      .as("test findcount with eq")
      .isEqualTo(1);

    dbModel = dbGmt.find(modelClass).where().eq(field, ref).findOne();
    softly.assertThat(dbModel)
      .as("test find with eq")
      .isNotNull();

    dbModel = dbGmt.find(modelClass).where().in(field, ref).findOne();
    softly.assertThat(dbModel)
      .as("test find with in")
      .isNotNull();
    softly.assertAll();
  }

  private Database createServer(String dbTimeZone) {

    DatabaseConfig config = new DatabaseConfig();
// create a clone from server() with different TZ
    config.setName(server().name());
    config.loadFromProperties(server().pluginApi().config().getProperties());
    config.setDataSource(server().dataSource());
    config.setReadOnlyDataSource(server().dataSource());
    config.setDdlGenerate(false);
    config.setDdlRun(false);
    config.setRegister(false);
    config.setDdlExtra(false);
    config.setDefaultServer(false);
    config.setChangeLogAsync(false);
    config.setDumpMetricsOnShutdown(false);
    config.setDataTimeZone(dbTimeZone);

    return DatabaseFactory.create(config);
  }
}
