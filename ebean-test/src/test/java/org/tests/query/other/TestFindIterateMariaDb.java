package org.tests.query.other;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.annotation.Platform;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.ForPlatform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test ensures that MariaDb uses the correct streaming result in findEach queries.
 * See Issue #56.
 */
public class TestFindIterateMariaDb extends BaseTestCase {

  @BeforeEach
  public void setup() {
    // we need at least > fetchSize beans
    if (DB.find(EBasic.class).findCount() < 1000) {
      for (int i = 0; i < 1000; i++) {
        EBasic dumbModel = new EBasic();
        dumbModel.setName("Goodbye now");
        DB.save(dumbModel);
      }
    }
  }

  public static class DtoBasic {
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @Test
  @ForPlatform(Platform.MARIADB)
  public void testStreamingOnSqlQueryFindEach() {

    try (Transaction txn = DB.beginTransaction()) {
      AtomicBoolean mariadbStreaming = new AtomicBoolean();
      DB.sqlQuery("select name from e_basic").findEach(bean -> {
        if (!mariadbStreaming.get() && isMariaDbStreaming()) {
          mariadbStreaming.set(true);
        }
      });
      assertThat(mariadbStreaming.get()).isTrue();
    }
  }

  @Test
  @ForPlatform(Platform.MARIADB)
  public void testStreamingOnOrmFindEach() {

    try (Transaction txn = DB.beginTransaction()) {
      AtomicBoolean mariadbStreaming = new AtomicBoolean();
      DB.find(EBasic.class).findEach(bean -> {
        if (!mariadbStreaming.get() && isMariaDbStreaming()) {
          mariadbStreaming.set(true);
        }
      });
      assertThat(mariadbStreaming.get()).isTrue();

    }
  }

  @Test
  @ForPlatform(Platform.MARIADB)
  public void testStreamingOnOrmAsDtoFindEach() {

    try (Transaction txn = DB.beginTransaction()) {
      AtomicBoolean mariadbStreaming = new AtomicBoolean();
      DB.find(EBasic.class).select("name").asDto(DtoBasic.class).findEach(bean -> {
        if (!mariadbStreaming.get() && isMariaDbStreaming()) {
          mariadbStreaming.set(true);
        }
      });
      assertThat(mariadbStreaming.get()).isTrue();

    }
  }

  @Test
  @ForPlatform(Platform.MARIADB)
  public void testStreamingOnDtoFindEach() {

    try (Transaction txn = DB.beginTransaction()) {
      AtomicBoolean mariadbStreaming = new AtomicBoolean();
      DB.findDto(DtoBasic.class, "select name from e_basic").findEach(bean -> {
        if (!mariadbStreaming.get() && isMariaDbStreaming()) {
          mariadbStreaming.set(true);
        }
      });
      assertThat(mariadbStreaming.get()).isTrue();

    }
  }
  @Test
  @ForPlatform(Platform.MARIADB)
  public void testStreamingOnDtoFindEachWhile() {

    try (Transaction txn = DB.beginTransaction()) {
      AtomicBoolean mariadbStreaming = new AtomicBoolean();
      DB.findDto(DtoBasic.class, "select name from e_basic").findEachWhile(bean -> {
        if (!mariadbStreaming.get() && isMariaDbStreaming()) {
          mariadbStreaming.set(true);
        }
        return false;
      });
      assertThat(mariadbStreaming.get()).isTrue();

    }
  }

  @Test
  @ForPlatform(Platform.MARIADB)
  public void testStreamingOnDtoFindEachBatch() {

    try (Transaction txn = DB.beginTransaction()) {
      AtomicBoolean mariadbStreaming = new AtomicBoolean();
      DB.findDto(DtoBasic.class, "select name from e_basic").findEach(2000, bean -> {
        if (!mariadbStreaming.get() && isMariaDbStreaming()) {
          mariadbStreaming.set(true);
        }
      });
      assertThat(mariadbStreaming.get()).isTrue();

    }
  }

  /**
   * Take a look into the current connection. We are streaming, if there is a result set.
   */
  private boolean isMariaDbStreaming() {
    try {
      org.mariadb.jdbc.Connection conn = Transaction.current().connection().unwrap(org.mariadb.jdbc.Connection.class);
      org.mariadb.jdbc.client.impl.StandardClient client = (org.mariadb.jdbc.client.impl.StandardClient) conn.getClient();
      Field field = client.getClass().getDeclaredField("streamMsg");
      field.setAccessible(true);
      return field.get(client) != null;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

}
