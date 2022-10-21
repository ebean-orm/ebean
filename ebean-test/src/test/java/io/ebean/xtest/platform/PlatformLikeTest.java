package io.ebean.xtest.platform;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.platform.cockroach.CockroachPlatform;
import io.ebean.platform.db2.DB2LuwPlatform;
import io.ebean.platform.h2.H2Platform;
import io.ebean.platform.hana.HanaPlatform;
import io.ebean.platform.mariadb.MariaDbPlatform;
import io.ebean.platform.mysql.MySql55Platform;
import io.ebean.platform.mysql.MySqlPlatform;
import io.ebean.platform.oracle.Oracle11Platform;
import io.ebean.platform.oracle.Oracle12Platform;
import io.ebean.platform.oracle.OraclePlatform;
import io.ebean.platform.postgres.Postgres9Platform;
import io.ebean.platform.postgres.PostgresPlatform;
import io.ebean.platform.sqlanywhere.SqlAnywherePlatform;
import io.ebean.platform.sqlite.SQLitePlatform;
import io.ebean.platform.sqlserver.SqlServer17Platform;
import io.ebean.platform.yugabyte.YugabytePlatform;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformLikeTest {

  DatabasePlatform h2 = new H2Platform();
  DatabasePlatform pg = new PostgresPlatform();
  DatabasePlatform pg9 = new Postgres9Platform();
  DatabasePlatform cockroach = new CockroachPlatform();
  DatabasePlatform yugabyte = new YugabytePlatform();
  DatabasePlatform mysql = new MySqlPlatform();
  DatabasePlatform mysql55 = new MySql55Platform();
  DatabasePlatform maria = new MariaDbPlatform();

  DatabasePlatform sqlite = new SQLitePlatform();
  DatabasePlatform sqlAnywhere = new SqlAnywherePlatform();
  DatabasePlatform sqlServer17 = new SqlServer17Platform();
  DatabasePlatform oracle = new OraclePlatform();
  DatabasePlatform oracle11 = new Oracle11Platform();
  DatabasePlatform oracle12 = new Oracle12Platform();
  DatabasePlatform hana = new HanaPlatform();
  DatabasePlatform db2Luw = new DB2LuwPlatform();

  @Test
  void rawLike_escapeEmpty() {
    for (DatabasePlatform platform : List.of(h2, pg, pg9, yugabyte, cockroach, mysql, mysql55, maria)) {
      assertThat(platform.likeClause(true)).isEqualTo("like ? escape''");
      assertThat(platform.likeClause(false)).isEqualTo("like ? escape'|'");
    }
  }

  @Test
  void ansi_rawLike() {
    for (DatabasePlatform platform : List.of(sqlite, sqlAnywhere, sqlServer17, oracle, oracle11, oracle12, hana, db2Luw)) {
      assertThat(platform.likeClause(true)).isEqualTo("like ?");
    }
  }

  @Test
  void escapeLike_noEscape() {
    for (DatabasePlatform platform : List.of(sqlite, sqlServer17)) {
      assertThat(platform.likeClause(false)).isEqualTo("like ?");
    }
  }


  @Test
  void ansi_escapeLike() {
    for (DatabasePlatform platform : List.of(sqlAnywhere, oracle, oracle11, oracle12, hana, db2Luw)) {
      assertThat(platform.likeClause(false)).isEqualTo("like ? escape'|'");
    }
  }

}
