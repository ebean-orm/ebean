package io.ebean.dbmigration;

import io.ebean.BaseTestCase;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.dbmigration.ddl.DdlRunner;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.PersistenceException;

public class DbMigrationTest extends BaseTestCase {

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
  @SuppressWarnings("deprecation")
  @Test
  public void testRunMigration() throws IOException {
    // first clean up previously created objects
    runScript(true, "drop table migtest_e_ref;\n","test");
    runScript(true, "alter table migtest_e_history set (system_versioning = off);\n" // mssql
        + "drop table migtest_e_history;\n"
        + "alter table migtest_e_history2 set (system_versioning = off);\n" // mssql
        + "drop table migtest_e_history2;\n"
        , "dropHistory");
    
    runScript(true, "drop table migtest_e_basic;\n"
        + "drop table migtest_e_history;\n"
        + "drop table migtest_e_ref;\n"
        + "drop table migtest_e_ref cascade;\n"
        + "drop table migtest_e_user;\n"
        // a bit tricky to delete history-table
        + "alter table migtest_e_history set (system_versioning = off);\n" // mssql
        + "drop table migtest_e_history;\n"
        + "drop table migtest_e_history_history;\n" // mssql
        + "drop table migtest_e_history2_history;\n" // mssql
        + "drop table migtest_e_history cascade;\n" // pg
        + "drop table migtest_e_history_history cascade;\n" // pg
        + "drop table migtest_e_softdelete;\n" // pg
        + "drop sequence migtest_e_basic_seq;\n"
        + "drop sequence migtest_e_history_seq;\n"
        + "drop sequence migtest_e_history2_seq;\n"
        + "drop sequence migtest_e_ref_seq;\n"
        + "drop sequence migtest_e_user_seq;\n"
        + "drop sequence migtest_e_history_seq;\n"
        + "drop sequence migtest_e_softdelete_seq;\n"
        + "alter table migtest_e_basic drop constraint ck_migtest_e_basic_status\n"
        , "cleanup");


    
    runScript(false, "1.0__initial.sql");
    
    SqlUpdate update = server().createSqlUpdate("insert into migtest_e_basic (id, old_boolean, user_id) values (1, :false, 1), (2, :true, 1)");
    update.setParameter("false", false);
    update.setParameter("true", true);
    
    assertThat(server().execute(update)).isEqualTo(2);

    
    update = server().createSqlUpdate("insert into migtest_e_history2 (id, test_string) values (1, 'Hello'), (2, 'foo'), (3, 'bar')");
    update.setParameter("str", "Hello");
    assertThat(server().execute(update)).isEqualTo(3);

    // Currently not supported to handle null columns in history
//    update = server().createSqlUpdate("update migtest_e_history2 set test_string = :str where id = 1");
//    update.setParameter("str", null);
//    assertThat(server().execute(update)).isEqualTo(1);
    update = server().createSqlUpdate("update migtest_e_history2 set test_string = :str where id = 1");
    update.setParameter("str", "World");
    assertThat(server().execute(update)).isEqualTo(1);

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
    
    assertThat(row.getString("new_string_field")).isEqualTo("foo'bar");
    assertThat(row.getBoolean("new_boolean_field2")).isTrue();
    assertThat(row.getTimestamp("some_date")).isEqualTo(new Timestamp(100, 0, 1, 0, 0, 0, 0)); // = 2000-01-01T00:00:00
    
    row = result.get(1);
    assertThat(row.getInteger("id")).isEqualTo(2);
    assertThat(row.getBoolean("old_boolean")).isTrue();
    assertThat(row.getBoolean("new_boolean_field")).isTrue(); // test if update old_boolean -> new_boolean_field works well
    
    assertThat(row.getString("new_string_field")).isEqualTo("foo'bar");
    assertThat(row.getBoolean("new_boolean_field2")).isTrue();
    assertThat(row.getTimestamp("some_date")).isEqualTo(new Timestamp(100, 0, 1, 0, 0, 0, 0)); // = 2000-01-01T00:00:00

    // Run migration & drops 
    runScript(false, "1.2__dropsFor_1.1.sql");

    
    select = server().createSqlQuery("select * from migtest_e_basic order by id");
    result = select.findList();
    assertThat(result).hasSize(2);
    row = result.get(0);
    assertThat(row.keySet()).doesNotContain("old_boolean", "old_boolean2");
    
    runScript(false, "1.3.sql");
    runScript(false, "1.4__dropsFor_1.3.sql");
    
    // now DB structure shoud be the same as v1_0
    select = server().createSqlQuery("select * from migtest_e_basic order by id");
    result = select.findList();
    assertThat(result).hasSize(2);
    row = result.get(0);
    assertThat(row.keySet()).contains("old_boolean", "old_boolean2");
  }

}
