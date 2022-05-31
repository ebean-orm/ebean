package org.tests.timezone;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TimestampTest {

  protected String platform="h2";
  protected Database db;

  @BeforeAll
  public void startTest() {
    db = createServer("GMT"); // test uses GMT database
  }

  @AfterAll
  public void shutdown() {
    if (db != null) {
      db.find(MTimestamp.class).delete();
      db.shutdown();
    }
  }

  /**
   * The test checks the write and read of LocalTime values. The database is in GMT time zone.
   * In order to verify the test in different java time zones (where the application runs),
   * use the <code>-Duser.timezone</code> as JVM argument,
   * e.g. <code>-Duser.timezone="America/New_York"</code> or <code>-Duser.timezone="PST"</code>>
   * or any other timezone: <a href="https://garygregory.wordpress.com/2013/06/18/what-are-the-java-timezone-ids/">https://garygregory.wordpress.com/2013/06/18/what-are-the-java-timezone-ids/</a>.
   */
  @Test
  public void testTimestamp() {
    Timestamp ts = new Timestamp(2021 - 1900, 11 - 1, 21, 5, 15, 15, 0);

    assertThat(db.find(MTimestamp.class).findCount()).isEqualTo(0);
    db.sqlUpdate("insert into mtimestamp (id, timestamp) values (1, '2021-11-21 05:15:15')").execute();

    int count = db.find(MTimestamp.class).where().eq("timestamp", ts).findCount();
    assertThat(count).isEqualTo(1);

    MTimestamp dbModel = db.find(MTimestamp.class).where().eq("timestamp", ts).findOne();
    assertThat(dbModel.getTimestamp().toString()).isEqualTo(ts.toString());
  }

  private Database createServer(String dbTimeZone) {
    DatabaseConfig config = new DatabaseConfig();
    config.setName(platform);
    config.loadFromProperties();
    config.setDdlExtra(false);
    config.setDefaultServer(false);
    config.setRegister(false);
    config.setChangeLogAsync(false);
    config.addClass(MTimestamp.class);

    config.setDumpMetricsOnShutdown(false);
    config.setDataTimeZone(dbTimeZone);

    return DatabaseFactory.create(config);
  }
}
