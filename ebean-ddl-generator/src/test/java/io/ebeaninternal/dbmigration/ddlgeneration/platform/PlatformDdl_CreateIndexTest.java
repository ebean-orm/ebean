package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebean.DB;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.hana.HanaPlatform;
import io.ebean.config.dbplatform.mysql.MySqlPlatform;
import io.ebean.config.dbplatform.oracle.OraclePlatform;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import io.ebeaninternal.dbmigration.ddlgeneration.PlatformDdlBuilder;
import io.ebeaninternal.dbmigration.migration.CreateIndex;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class PlatformDdl_CreateIndexTest {

  private final PlatformDdl h2Ddl = PlatformDdlBuilder.create(new H2Platform());
  private final PlatformDdl pgDdl = PlatformDdlBuilder.create(new PostgresPlatform());
  private final PlatformDdl mysqlDdl = PlatformDdlBuilder.create(new MySqlPlatform());
  private final PlatformDdl oraDdl = PlatformDdlBuilder.create(new OraclePlatform());
  private final PlatformDdl sqlServerDdl = PlatformDdlBuilder.create(new SqlServer17Platform());
  private final PlatformDdl hanaDdl = PlatformDdlBuilder.create(new HanaPlatform());

  {
    DatabaseConfig config = DB.getDefault().getPluginApi().getServerConfig();
    h2Ddl.configure(config);
    pgDdl.configure(config);
    mysqlDdl.configure(config);
    oraDdl.configure(config);
    sqlServerDdl.configure(config);
    hanaDdl.configure(config);
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

}
