package org.tests.timezone;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InstantTest {

  protected String platform="h2";
  protected Database db;

  @BeforeAll
  public void startTest() {
    db = createServer("GMT"); // test uses GMT database
  }

  @AfterAll
  public void shutdown() {
    if (db != null) {
      db.find(MInstant.class).delete();
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
  public void testInstant() {
    Instant inst = Instant.parse("2021-11-21T05:15:15Z");

    assertThat(db.find(MInstant.class).findCount()).isEqualTo(0);
    db.sqlUpdate("insert into minstant (id, instant) values (1, '2021-11-21 05:15:15')").execute();

    int count = db.find(MInstant.class).where().eq("instant", inst).findCount();
    assertThat(count).isEqualTo(1);

    MInstant dbModel = db.find(MInstant.class).where().eq("instant", inst).findOne();
    assertThat(dbModel.getInstant().toString()).isEqualTo(inst.toString());
  }

  private Database createServer(String dbTimeZone) {
    DatabaseConfig config = new DatabaseConfig();
    config.setName(platform);
    config.loadFromProperties();
    config.setDdlExtra(false);
    config.setDefaultServer(false);
    config.setRegister(false);
    config.setChangeLogAsync(false);
    config.addClass(MInstant.class);

    config.setDumpMetricsOnShutdown(false);
    config.setDataTimeZone(dbTimeZone);

    return DatabaseFactory.create(config);
  }
}
