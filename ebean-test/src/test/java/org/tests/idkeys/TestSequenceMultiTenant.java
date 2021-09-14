package org.tests.idkeys;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.multitenant.partition.UserContext;
import org.tests.idkeys.db.GenKeySequence;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.TenantDataSourceProvider;
import io.ebean.config.TenantMode;
import io.ebean.config.TenantSchemaProvider;
import io.ebean.config.dbplatform.h2.H2Platform;

/**
 * Tests sequences for multiple tenants.
 */
public class TestSequenceMultiTenant {

  /**
   * Tests sequences using multi tenancy per database
   */
  @Test
  public void test_multi_tenant_db_sequences() {

    Database db = setupDb();

    UserContext.set("4711", "1");
    assertEquals(1L, db.nextId(GenKeySequence.class));
    assertEquals(2L, db.nextId(GenKeySequence.class));

    UserContext.set("5711", "2");
    assertEquals(1L, db.nextId(GenKeySequence.class));

    UserContext.set("4711", "1");
    assertEquals(3L, db.nextId(GenKeySequence.class));

  }

  private static Database setupDb() {

    DatabaseConfig config = new DatabaseConfig();

    config.setName("h2multitenantseq");
    config.loadFromProperties();
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDdlExtra(false);
    config.setRegister(false);
    config.setDefaultServer(false);
    config.setCurrentTenantProvider(() -> UserContext.get().getTenantId());
    config.setTenantMode(TenantMode.DB);
    config.setDatabasePlatform(new H2Platform());
    config.setTenantDataSourceProvider(new TenantDataSourceProvider() {

      Map<Object, DataSource> map = new ConcurrentHashMap<>();

      @Override
      public DataSource dataSource(Object tenantId) {
        if (tenantId == null) {
          tenantId = "1";
        }
        return map.computeIfAbsent(tenantId, this::createDataSource);
      }

      private DataSource createDataSource(Object tenantId) {

        DatabaseConfig config = new DatabaseConfig();

        config.setName("h2multitenantseq");
        config.loadFromProperties();
        config.setDdlRun(true);
        config.setDdlExtra(false);
        config.setRegister(false);
        config.setDefaultServer(false);
        config.getDataSourceConfig().setUrl("jdbc:h2:mem:h2multitenantseq-" + tenantId);

        return DatabaseFactory.create(config).pluginApi().dataSource();
      }
    });

    config.getClasses().add(GenKeySequence.class);

    return DatabaseFactory.create(config);
  }

  
  /**
   * Tests sequences using multi tenancy per schema
   */
  @Test
  public void test_multi_tenant_schema_sequences() throws SQLException {
    createDDl("PUBLIC");
    createDDl("TENANT_SCHEMA_1");
    createDDl("TENANT_SCHEMA_2");

    Database db = setupSchema();

    UserContext.set("4711", "1");
    assertEquals(1L, db.nextId(GenKeySequence.class));
    assertEquals(2L, db.nextId(GenKeySequence.class));

    UserContext.set("5711", "2");
    assertEquals(1L, db.nextId(GenKeySequence.class));

    UserContext.set("4711", "1");
    assertEquals(3L, db.nextId(GenKeySequence.class));
  }

  private Database setupSchema() {

    DatabaseConfig config = new DatabaseConfig();

    config.setName("h2multitenantseq");
    config.loadFromProperties();
    config.setDdlGenerate(true);
    config.setDdlRun(false);
    config.setDdlExtra(false);
    config.setRegister(false);
    config.setDefaultServer(false);
    config.setCurrentTenantProvider(() -> UserContext.get().getTenantId());
    config.setTenantMode(TenantMode.SCHEMA);
    config.setDatabasePlatform(new H2Platform());
    config.getDataSourceConfig().setUrl("jdbc:h2:mem:h2multitenantseq;DB_CLOSE_ON_EXIT=FALSE;");
    config.setTenantSchemaProvider(new TenantSchemaProvider() {

      @Override
      public String schema(Object tenantId) {
        return tenantId == null
            ? "TENANT_SCHEMA_1"
            : "TENANT_SCHEMA_1" + tenantId;
      }
    });

    config.getClasses().add(GenKeySequence.class);

    return DatabaseFactory.create(config);
  }
  
  private void createDDl(String schema) {
    DatabaseConfig config = new DatabaseConfig();

    config.setName("h2multitenantseq");
    config.loadFromProperties();
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDdlExtra(false);
    config.setRegister(false);
    config.setDefaultServer(false);
    config.getDataSourceConfig().setUrl(
        "jdbc:h2:mem:h2multitenantseq;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS "
            + schema + "\\;SET SCHEMA " + schema);

    config.getClasses().add(GenKeySequence.class);

    DatabaseFactory.create(config).shutdown();
  }
  
}
