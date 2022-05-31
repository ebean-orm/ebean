package org.tests.timezone;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocalDateTimeTest {

  protected String platform="h2";
  protected Database db;

  @BeforeAll
  public void startTest() {
    db = createServer("GMT"); // test uses GMT database
  }

  @AfterAll
  public void shutdown() {
    if (db != null) {
      db.find(MLocalDateTime.class).delete();
      db.shutdown();
    }
  }

  @Test
  public void testLocalDateTime() {
    LocalDateTime ldt = LocalDateTime.parse("2021-11-21T05:15:15");
    assertThat(db.find(MLocalDateTime.class).findCount()).isEqualTo(0);
    db.sqlUpdate("insert into mlocal_date_time (id, local_date_time) values (1, '2021-11-21 05:15:15')").execute();

    int count = db.find(MLocalDateTime.class).where().eq("local_date_time", ldt).findCount();
    assertThat(count).isEqualTo(1);

    MLocalDateTime dbModel = db.find(MLocalDateTime.class).where().eq("local_date_time", ldt).findOne();
    assertThat(dbModel.getLocalDateTime().toString()).isEqualTo(ldt.toString());
  }

  private Database createServer(String dbTimeZone) {
    DatabaseConfig config = new DatabaseConfig();
    config.setName(platform);
    config.loadFromProperties();
    config.setDdlExtra(false);
    config.setDefaultServer(false);
    config.setRegister(false);
    config.setChangeLogAsync(false);
    config.addClass(MLocalDateTime.class);

    config.setDumpMetricsOnShutdown(false);
    config.setDataTimeZone(dbTimeZone);

    return DatabaseFactory.create(config);
  }
}
