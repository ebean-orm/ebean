package io.ebean.dbmigration;

import io.ebean.BaseTestCase;
import io.ebean.SqlQuery;
import io.ebean.SqlRow;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.migration.ddl.DdlRunner;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.PersistenceException;

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
  public void testRunMigration() throws IOException {
    // first clean up previously created objects
    cleanup("migtest_ckey_assoc",
        "migtest_ckey_detail",
        "migtest_ckey_parent",
        "migtest_e_basic",
        "migtest_e_history",
        "migtest_e_history2",
        "migtest_e_ref",
        "migtest_e_softdelete",
        "migtest_e_user",
        "migtest_mtm_c",
        "migtest_mtm_m",
        "migtest_mtm_c_migtest_mtm_m",
        "migtest_mtm_m_migtest_mtm_c",
        "migtest_oto_child",
        "migtest_oto_master");
    

    runScript(false, "1.0__initial.sql");

    if (isOracle()) {
      SqlUpdate update = server().createSqlUpdate("insert into migtest_e_basic (id, old_boolean, user_id) values (1, :false, 1)");
      update.setParameter("false", false);
      assertThat(server().execute(update)).isEqualTo(1);
      
      update = server().createSqlUpdate("insert into migtest_e_basic (id, old_boolean, user_id) values (2, :true, 1)");
      update.setParameter("true", true);
      assertThat(server().execute(update)).isEqualTo(1);
    } else {
      SqlUpdate update = server().createSqlUpdate("insert into migtest_e_basic (id, old_boolean, user_id) values (1, :false, 1), (2, :true, 1)");
      update.setParameter("false", false);
      update.setParameter("true", true);
  
      assertThat(server().execute(update)).isEqualTo(2);
    }

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
    if (isMySql()) {
      return; // TODO: mysql cannot drop table (need stored procedure for drop table)
    }
    runScript(false, "1.2__dropsFor_1.1.sql");


    // Oracle caches the statement and does not detect schema change. It fails with
    // an ORA-01007
    select = server().createSqlQuery("select * from migtest_e_basic order by id,id"); 
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

  private void cleanup(String ... tables) {
    StringBuilder sb = new StringBuilder();
    for (String table : tables) {
      // simple and stupid try to execute all commands on all dialects.
      sb.append("alter table ").append(table).append(" set ( system_versioning = OFF  );\n");
      sb.append("drop table ").append(table).append(";\n");
      sb.append("drop table ").append(table).append(" cascade;\n");
      sb.append("drop table ").append(table).append("_history;\n");
      sb.append("drop table ").append(table).append("_history cascade;\n");
      sb.append("drop view ").append(table).append("_with_history;\n");
      sb.append("drop sequence ").append(table).append("_seq;\n");
    }
    
    runScript(true, sb.toString(), "cleanup");
    runScript(true, sb.toString(), "cleanup");

  }

}
