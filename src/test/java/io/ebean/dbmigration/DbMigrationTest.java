package io.ebean.dbmigration;

import io.ebean.BaseTestCase;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.Platform;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.config.ServerConfig;
import io.ebean.dbmigration.ddl.DdlRunner;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.persistence.PersistenceException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DbMigrationTest extends BaseTestCase {

  private static final Logger logger = LoggerFactory.getLogger(DbMigrationTest.class);


  private int runScript(boolean expectErrors, String scriptName) throws IOException {
    try (InputStream stream = getClass().getResourceAsStream("/dbmigration/migrationtest/" + server().getPluginApi().getDatabasePlatform().getName()+"/" + scriptName);
      java.util.Scanner s = new java.util.Scanner(stream)) {
      s.useDelimiter("\\A");
      if (s.hasNext()) {
        return runScript(expectErrors, s.next(), scriptName);
      }
    }
    return 0;
  }

  private int runScript(boolean expectErrors, String content, String scriptName) {

    DdlRunner runner = new DdlRunner(expectErrors, scriptName);

    Transaction transaction = server().createTransaction();
    Connection connection = transaction.getConnection();
    try {
      if (expectErrors) {
        connection.setAutoCommit(true);
      }
      int count = runner.runAll(content, connection);
      if (expectErrors) {
        connection.setAutoCommit(false);
      }
      transaction.commit();
      return count;

    } catch (SQLException e) {
      throw new PersistenceException("Failed to run script", e);

    } finally {
      transaction.end();
    }
  }
  @Test
  public void testMigration() throws IOException {
    // first clean up previously created objects
    runScript(true, "drop table migtest_e_basic;", "cleanup");
    runScript(true, "drop sequence migtest_e_basic_seq;", "cleanup");
    
    
    runScript(false, "1.0__initial.sql");
    
    SqlUpdate update = server().createSqlUpdate("insert into migtest_e_basic (id, old_boolean) values (1, :false), (2, :true)");
    update.setParameter("false", false);
    update.setParameter("true", true);
    
    assertThat(server().execute(update)).isEqualTo(2);

    // Run migration
    runScript(false, "1.1.sql");
    SqlQuery select = server().createSqlQuery("select * from migtest_e_basic order by id");
    List<SqlRow> result = select.findList();
    assertThat(result).hasSize(2);
    
    SqlRow row = result.get(0);
    assertThat(row.keySet()).contains("old_boolean", "old_boolean2");
    
    assertThat(row.getInteger("id")).isEqualTo(1);
    assertThat(row.getBoolean("old_boolean")).isFalse();
    assertThat(row.getBoolean("new_boolean_field")).isFalse(); // test if update old_boolean -> new_boolean_field works well
    
    assertThat(row.getString("new_string_field")).isEqualTo("foo");
    assertThat(row.getBoolean("new_boolean_field2")).isTrue();
    assertThat(row.getTimestamp("some_date")).isEqualTo(new Timestamp(100, 0, 1, 0, 0, 0, 0)); // = 2000-01-01T00:00:00
    
    row = result.get(1);
    assertThat(row.getInteger("id")).isEqualTo(2);
    assertThat(row.getBoolean("old_boolean")).isTrue();
    assertThat(row.getBoolean("new_boolean_field")).isTrue(); // test if update old_boolean -> new_boolean_field works well
    
    assertThat(row.getString("new_string_field")).isEqualTo("foo");
    assertThat(row.getBoolean("new_boolean_field2")).isTrue();
    assertThat(row.getTimestamp("some_date")).isEqualTo(new Timestamp(100, 0, 1, 0, 0, 0, 0)); // = 2000-01-01T00:00:00

    // Run drops
    runScript(false, "1.2__dropsFor_1.1.sql");

    select = server().createSqlQuery("select * from migtest_e_basic order by id");
    result = select.findList();
    assertThat(result).hasSize(2);
    row = result.get(0);
    assertThat(row.keySet()).doesNotContain("old_boolean", "old_boolean2");
  }

}
