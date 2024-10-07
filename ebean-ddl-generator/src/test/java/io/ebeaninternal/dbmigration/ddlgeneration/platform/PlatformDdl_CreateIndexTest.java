package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.DB;
import io.ebean.DatabaseBuilder;
import io.ebean.platform.db2.DB2LuwPlatform;
import io.ebean.platform.h2.H2Platform;
import io.ebean.platform.hana.HanaPlatform;
import io.ebean.platform.mysql.MySqlPlatform;
import io.ebean.platform.oracle.OraclePlatform;
import io.ebean.platform.postgres.PostgresPlatform;
import io.ebean.platform.sqlserver.SqlServer17Platform;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import io.ebeaninternal.dbmigration.migration.CreateIndex;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlatformDdl_CreateIndexTest {

  private final PlatformDdl h2Ddl = PlatformDdlBuilder.create(new H2Platform());
  private final PlatformDdl pgDdl = PlatformDdlBuilder.create(new PostgresPlatform());
  private final PlatformDdl mysqlDdl = PlatformDdlBuilder.create(new MySqlPlatform());
  private final PlatformDdl oraDdl = PlatformDdlBuilder.create(new OraclePlatform());
  private final PlatformDdl sqlServerDdl = PlatformDdlBuilder.create(new SqlServer17Platform());
  private final PlatformDdl hanaDdl = PlatformDdlBuilder.create(new HanaPlatform());
  private final PlatformDdl db2LuwDdl = PlatformDdlBuilder.create(new DB2LuwPlatform());

  {
    DatabaseBuilder.Settings config = DB.getDefault().pluginApi().config();
    h2Ddl.configure(config);
    pgDdl.configure(config);
    mysqlDdl.configure(config);
    oraDdl.configure(config);
    sqlServerDdl.configure(config);
    hanaDdl.configure(config);
    db2LuwDdl.configure(config);
  }

  WriteCreateIndex writeCreateIndex() {
    return writeCreateIndex(true, true);
  }

  WriteCreateIndex writeCreateIndex(boolean unique, boolean concurrent) {
    CreateIndex createIndex = new CreateIndex();
    createIndex.setIndexName("ix_mytab_acol");
    createIndex.setTableName("mytab");
    createIndex.setColumns("acol");
    createIndex.setUnique(unique);
    createIndex.setConcurrent(concurrent);
    return new WriteCreateIndex(createIndex);
  }

  WriteCreateIndex fkeyCreateIndex(boolean unique) {
    return new WriteCreateIndex("ix_mytab_acol", "mytab", new String[]{"acol"}, unique);
  }


  @Test
  public void createUniqueIndex() {

    WriteCreateIndex createIndex = writeCreateIndex();

    String sql = h2Ddl.createIndex(createIndex);
    assertEquals("create unique index ix_mytab_acol on mytab (acol)", sql);
    sql = pgDdl.createIndex(createIndex);
    assertEquals("create unique index concurrently if not exists ix_mytab_acol on mytab (acol)", sql);
    sql = mysqlDdl.createIndex(createIndex);
    assertEquals("create unique index ix_mytab_acol on mytab (acol)", sql);
    sql = sqlServerDdl.createIndex(createIndex);
    assertEquals("create unique index ix_mytab_acol on mytab (acol)", sql);
    sql = oraDdl.createIndex(createIndex);
    assertEquals("create unique index ix_mytab_acol on mytab (acol)", sql);
    sql = hanaDdl.createIndex(createIndex);
    assertThat(sql).isEqualTo("-- explicit index \"ix_mytab_acol\" for single column \"acol\" of table \"mytab\" is not necessary");
    sql = db2LuwDdl.createIndex(createIndex);
    assertThat(sql).isEqualTo("create unique index ix_mytab_acol on mytab (acol)");
  }

  @Test
  public void postgres_createIndex() {

    String sql = pgDdl.createIndex(writeCreateIndex(true, true));
    assertEquals("create unique index concurrently if not exists ix_mytab_acol on mytab (acol)", sql);
    sql = pgDdl.createIndex(writeCreateIndex(false, false));
    assertEquals("create index if not exists ix_mytab_acol on mytab (acol)", sql);
    sql = pgDdl.createIndex(writeCreateIndex(true, false));
    assertEquals("create unique index if not exists ix_mytab_acol on mytab (acol)", sql);
    sql = pgDdl.createIndex(writeCreateIndex(false, true));
    assertEquals("create index concurrently if not exists ix_mytab_acol on mytab (acol)", sql);
  }

  @Test
  public void postgres_fkeyCreateIndex() {
    String sql = pgDdl.createIndex(fkeyCreateIndex(true));
    assertEquals("create unique index ix_mytab_acol on mytab (acol)", sql);

    sql = pgDdl.createIndex(fkeyCreateIndex(false));
    assertEquals("create index ix_mytab_acol on mytab (acol)", sql);
  }

  @Test
  public void db2luw_fkeyCreateIndex() {
    String sql = db2LuwDdl.createIndex(fkeyCreateIndex(true));
    assertEquals("create unique index ix_mytab_acol on mytab (acol)", sql);

    sql = db2LuwDdl.createIndex(fkeyCreateIndex(false));
    assertEquals("create index ix_mytab_acol on mytab (acol)", sql);
  }

  @Test
  public void db2luw_tablespaceIndex() {
    String sql = db2LuwDdl.createIndex(new WriteCreateIndex("ix_mytab_acol", "mytab", new String[]{"acol"}, false));
    assertEquals("create index ix_mytab_acol on mytab (acol)", sql);
    sql = db2LuwDdl.createIndex(new WriteCreateIndex("ix_mytab_acol", "mytab", new String[]{"acol"}, true));
    assertEquals("create unique index ix_mytab_acol on mytab (acol)", sql);
  }

}
