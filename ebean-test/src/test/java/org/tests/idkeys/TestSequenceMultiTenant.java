package org.tests.idkeys;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.multitenant.partition.UserContext;
import org.tests.idkeys.db.GenKeySeqA;

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
class TestSequenceMultiTenant {

  /**
   * Tests sequences using multi tenancy per database
   */
  @Test
  void test_multi_tenant_db_sequences() {

    Database db = setupDb();

    UserContext.set("4711", "1");
    assertEquals(1L, db.nextId(GenKeySeqA.class));
    assertEquals(2L, db.nextId(GenKeySeqA.class));

    UserContext.set("5711", "2");
    assertEquals(1L, db.nextId(GenKeySeqA.class));

    UserContext.set("4711", "1");
    assertEquals(3L, db.nextId(GenKeySeqA.class));

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

    config.getClasses().add(GenKeySeqA.class);

    return DatabaseFactory.create(config);
  }
  
  /**
   * Tests sequences using multi tenancy per schema
   */
  @Test
  void test_multi_tenant_schema_sequences() throws SQLException {
    createDDl("PUBLIC");
    createDDl("TENANT_SCHEMA_1");
    createDDl("TENANT_SCHEMA_2");

    Database db = setupSchema();
    
    Connection connection = db.dataSource().getConnection();
    
    // debugging schemas
    // see org.h2.jdbc.JdbcDatabaseMetaData.getSchemas()
    ResultSet rs = connection.getMetaData().getSchemas();
    String[] schemaData = new String[4];
    int schemaCnt = 0;
    while(rs.next()) {
      schemaData[schemaCnt++] = String.format("%s, %s, %s", rs.getString(1), rs.getString(2), rs.getBoolean(3));
    }
    assertArrayEquals(schemaData, new String[]{"INFORMATION_SCHEMA, H2MULTITENANTSEQ, false", 
      "PUBLIC, H2MULTITENANTSEQ, true", 
      "TENANT_SCHEMA_1, H2MULTITENANTSEQ, false", 
      "TENANT_SCHEMA_2, H2MULTITENANTSEQ, false"});
    
    // debugging sequences
    rs = connection.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.SEQUENCES;").executeQuery();
    String[] sequenceData = new String[4];
    int sequenceCnt = 0;
    while (rs.next()) {
      // SEQUENCE_CATALOG,SEQUENCE_SCHEMA,SEQUENCE_NAME,CURRENT_VALUE,INCREMENT,IS_GENERATED,REMARKS,CACHE,MIN_VALUE,MAX_VALUE,IS_CYCLE,ID
      String format = String.format("%s, %s, %s", rs.getString(1), rs.getString(2), rs.getString(3));
      System.out.println(format);
      sequenceData[sequenceCnt++] = format;
    }

    UserContext.set("4711", "1");
    assertEquals(1L, db.nextId(GenKeySeqA.class));
    assertEquals(2L, db.nextId(GenKeySeqA.class));

    UserContext.set("5711", "2");
    assertEquals(1L, db.nextId(GenKeySeqA.class));

    UserContext.set("4711", "1");
    assertEquals(3L, db.nextId(GenKeySeqA.class));
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
            ? "PUBLIC"
            : "TENANT_SCHEMA_" + tenantId;
      }
    });

    config.getClasses().add(GenKeySeqA.class);

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
    config.setDbSchema(schema); // see io.ebeaninternal.dbmigration.DdlGenerator.createSchemaIfRequired(Connection)

    config.getClasses().add(GenKeySeqA.class);

    DatabaseFactory.create(config);
  }
  
}
