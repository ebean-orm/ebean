package io.ebeaninternal.dbmigration;

import io.ebean.BaseTestCase;
import io.ebean.SqlRow;
import io.ebean.SqlUpdate;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebeaninternal.dbmigration.ddlgeneration.Helper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DbMigrationTest extends BaseTestCase {

  private void runScript(boolean expectErrors, String scriptName) throws IOException {
    String ddl = Helper.asText(this, "/dbmigration/migrationtest/" + server().platform().name().toLowerCase() + "/" + scriptName);
    runScript(expectErrors, ddl, scriptName);
  }

  private void runScript(boolean useAutoCommit, String content, String scriptName) {
    server().script().runScript(scriptName, content, useAutoCommit);
  }

  @IgnorePlatform({Platform.ORACLE, Platform.NUODB, Platform.MARIADB})
  @Test
  public void testRunMigration() throws IOException {
    // first clean up previously created objects
    cleanup("migtest_ckey_assoc",
        "migtest_ckey_detail",
        "migtest_ckey_parent",
        "migtest_e_basic",
        "migtest_e_enum",
        "migtest_e_history",
        "migtest_e_history2",
        "migtest_e_history3",
        "migtest_e_history4",
        "migtest_e_history5",
        "migtest_e_history6",
        "migtest_e_ref",
        "migtest_e_softdelete",
        "migtest_e_user",
        "migtest_fk_cascade",
        "migtest_fk_cascade_one",
        "migtest_fk_none",
        "migtest_fk_none_via_join",
        "migtest_fk_one",
        "migtest_fk_set_null",
        "migtest_mtm_c",
        "migtest_mtm_m",
        "migtest_mtm_c_migtest_mtm_m",
        "migtest_mtm_m_migtest_mtm_c",
        "migtest_oto_child",
        "migtest_oto_master");

    if (isSqlServer()) { //  || isMySql()
      runScript(false, "I__create_procs.sql");
    }

    runScript(false, "1.0__initial.sql");

    if (isOracle() || isHana()) {
      SqlUpdate update = server().sqlUpdate("insert into migtest_e_basic (id, old_boolean, user_id) values (1, :false, 1)");
      update.setParameter("false", false);
      assertThat(server().execute(update)).isEqualTo(1);

      update = server().sqlUpdate("insert into migtest_e_basic (id, old_boolean, user_id) values (2, :true, 1)");
      update.setParameter("true", true);
      assertThat(server().execute(update)).isEqualTo(1);
    } else {
      SqlUpdate update = server().sqlUpdate("insert into migtest_e_basic (id, old_boolean, user_id) values (1, :false, 1), (2, :true, 1)");
      update.setParameter("false", false);
      update.setParameter("true", true);

      assertThat(server().execute(update)).isEqualTo(2);
    }

    createHistoryEntities();

    // Run migration
    runScript(false, "1.1.sql");
    List<SqlRow> result = server().sqlQuery("select * from migtest_e_basic order by id").findList();
    assertThat(result).hasSize(2);

    SqlRow row = result.get(0);
    assertThat(row.keySet()).contains("old_boolean", "old_boolean2");

    assertThat(row.getInteger("id")).isEqualTo(1);
    assertThat(row.getBoolean("old_boolean")).isFalse();
    assertThat(row.getBoolean("new_boolean_field")).isFalse(); // test if update old_boolean -> new_boolean_field works well

    assertThat(row.getString("new_string_field")).isEqualTo("foo'bar");
    assertThat(row.getBoolean("new_boolean_field2")).isTrue();
    //assertThat(row.getTimestamp("some_date")).isCloseTo(new Date(), 86_000); // allow 1 minute delta

    row = result.get(1);
    assertThat(row.getInteger("id")).isEqualTo(2);
    assertThat(row.getBoolean("old_boolean")).isTrue();
    assertThat(row.getBoolean("new_boolean_field")).isTrue(); // test if update old_boolean -> new_boolean_field works well

    assertThat(row.getString("new_string_field")).isEqualTo("foo'bar");
    assertThat(row.getBoolean("new_boolean_field2")).isTrue();
    //assertThat(row.getTimestamp("some_date")).isCloseTo(new Date(), 60_000); // allow 1 minute delta

    runScript(false, "1.2__dropsFor_1.1.sql");

    // Oracle caches the statement and does not detect schema change. It fails with
    // an ORA-01007
    if (isOracle()) {
      result = server().sqlQuery("select * from migtest_e_basic order by id,id").findList();
    } else {
      result = server().sqlQuery("select * from migtest_e_basic order by id").findList();
    }
    assertThat(result).hasSize(2);
    row = result.get(0);
    assertThat(row.keySet()).doesNotContain("old_boolean", "old_boolean2");

    runScript(false, "1.3.sql");
    runScript(false, "1.4__dropsFor_1.3.sql");

    // now DB structure shoud be the same as v1_0
    result = server().sqlQuery("select * from migtest_e_basic order by id").findList();
    assertThat(result).hasSize(2);
    row = result.get(0);
    assertThat(row.keySet()).contains("old_boolean", "old_boolean2");
  }

  /**
   *
   */
  private void createHistoryEntities() {
    SqlUpdate update = server().sqlUpdate("insert into migtest_e_history (id, test_string) values (1, '42')");
    assertThat(server().execute(update)).isEqualTo(1);
    update = server().sqlUpdate("update migtest_e_history set test_string = '45' where id = 1");
    assertThat(server().execute(update)).isEqualTo(1);

    update = server().sqlUpdate("insert into migtest_e_history2 (id, test_string, obsolete_string1, obsolete_string2) values (1, 'foo', 'bar', null)");
    assertThat(server().execute(update)).isEqualTo(1);
    update = server().sqlUpdate("update migtest_e_history2 set test_string = 'baz' where id = 1");
    assertThat(server().execute(update)).isEqualTo(1);

    update = server().sqlUpdate("insert into migtest_e_history3 (id, test_string) values (1, '42')");
    assertThat(server().execute(update)).isEqualTo(1);
    update = server().sqlUpdate("update migtest_e_history3 set test_string = '45' where id = 1");
    assertThat(server().execute(update)).isEqualTo(1);

    update = server().sqlUpdate("insert into migtest_e_history4 (id, test_number) values (1, 42)");
    assertThat(server().execute(update)).isEqualTo(1);
    update = server().sqlUpdate("update migtest_e_history4 set test_number = 45 where id = 1");
    assertThat(server().execute(update)).isEqualTo(1);

    update = server().sqlUpdate("insert into migtest_e_history5 (id, test_number) values (1, 42)");
    assertThat(server().execute(update)).isEqualTo(1);
    update = server().sqlUpdate("update migtest_e_history5 set test_number = 45 where id = 1");
    assertThat(server().execute(update)).isEqualTo(1);

    update = server().sqlUpdate("insert into migtest_e_history6 (id, test_number1, test_number2) values (1, 2, 7)");
    assertThat(server().execute(update)).isEqualTo(1);
    update = server().sqlUpdate("update migtest_e_history6 set test_number2 = 45 where id = 1");
    assertThat(server().execute(update)).isEqualTo(1);
  }

  private void cleanup(String ... tables) {

    final boolean sqlServer = isSqlServer();
    final boolean postgres = isPostgres();

    StringBuilder sb = new StringBuilder();
    for (String table : tables) {
      // simple and stupid try to execute all commands on all dialects.
      if (sqlServer) {
        sb.append("alter table ").append(table).append(" set ( system_versioning = OFF  );\n");
        sb.append("alter table ").append(table).append(" drop system versioning;\n");
      }
      if (postgres) {
        sb.append("drop table ").append(table).append(" cascade;\n");
        sb.append("drop table ").append(table).append("_history cascade;\n");
      } else {
        sb.append("drop table ").append(table).append(";\n");
        sb.append("drop table ").append(table).append("_history;\n");
      }
      sb.append("drop view ").append(table).append("_with_history;\n");
      sb.append("drop sequence ").append(table).append("_seq;\n");
    }

    runScript(true, sb.toString(), "cleanup");
    runScript(true, sb.toString(), "cleanup");
  }
}
