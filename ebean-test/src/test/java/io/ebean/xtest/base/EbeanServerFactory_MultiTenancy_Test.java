package io.ebean.xtest.base;

import io.ebean.DatabaseBuilder;
import io.ebean.xtest.BaseTestCase;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.*;
import io.ebean.platform.mysql.MySqlPlatform;
import io.ebean.platform.postgres.PostgresPlatform;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;

public class EbeanServerFactory_MultiTenancy_Test extends BaseTestCase {

  /**
   *  Tests using multi tenancy per database
   */
  @Test
  public void create_new_server_with_multi_tenancy_db() {

    String tenant = "customer";
    CurrentTenantProvider tenantProvider = Mockito.mock(CurrentTenantProvider.class);
    Mockito.doReturn(tenant).when(tenantProvider).currentId();

    DataSource mockedDataSource = Mockito.mock(DataSource.class);
    TenantDataSourceProvider dataSourceProvider = Mockito.mock(TenantDataSourceProvider.class);
    Mockito.doReturn(mockedDataSource).when(dataSourceProvider).dataSource(tenant);

    DatabaseBuilder config = new DatabaseConfig();
    config.setName("multiTenantDb");
    config.loadFromProperties();
    config.setRegister(false);
    config.setDefaultServer(false);

    config.setTenantMode(TenantMode.DB);
    config.setCurrentTenantProvider(tenantProvider);
    config.setTenantDataSourceProvider(dataSourceProvider);

    // When TenantMode.DB we don't really want to run DDL
    // and we want to explicitly specify the Database platform
    //config.setDdlGenerate(false);
    //config.setDdlRun(false);
    config.setDatabasePlatform(new PostgresPlatform());

    final Database database = DatabaseFactory.create(config);
    database.shutdown();
  }



  /**
   *  Tests using multi tenancy per schema
   */
  @Test
  public void create_new_server_with_multi_tenancy_db_with_master() {

    String tenant = "customer";
    CurrentTenantProvider tenantProvider = Mockito.mock(CurrentTenantProvider.class);
    Mockito.doReturn(tenant).when(tenantProvider).currentId();

    TenantDataSourceProvider dataSourceProvider = Mockito.mock(TenantDataSourceProvider.class);

    DatabaseConfig config = new DatabaseConfig();

    config.setName("h2");
    config.loadFromProperties();
    config.setRegister(false);
    config.setDefaultServer(false);
    config.setDdlGenerate(false);
    config.setDdlRun(false);

    config.setTenantMode(TenantMode.DB_WITH_MASTER);
    config.setCurrentTenantProvider(tenantProvider);
    config.setTenantDataSourceProvider(dataSourceProvider);

    Mockito.doReturn(config.getDataSource()).when(dataSourceProvider).dataSource(tenant);

    config.setDatabasePlatform(new PostgresPlatform());

    final Database database = DatabaseFactory.create(config);
    database.shutdown();
  }

  /**
   *  Tests using multi tenancy per schema
   */
  @Test
  public void create_new_server_with_multi_tenancy_schema() {

    String tenant = "customer";
    CurrentTenantProvider tenantProvider = Mockito.mock(CurrentTenantProvider.class);
    Mockito.doReturn(tenant).when(tenantProvider).currentId();

    TenantSchemaProvider schemaProvider = Mockito.mock(TenantSchemaProvider.class);
    Mockito.doReturn("tenant_schema").when(schemaProvider).schema(tenant);

    DatabaseBuilder config = new DatabaseConfig();
    config.setName("h2");
    config.loadFromProperties();
    config.setName("multi-tenancy");
    config.setRegister(false);
    config.setDefaultServer(false);

    config.setTenantMode(TenantMode.SCHEMA);
    config.setCurrentTenantProvider(tenantProvider);
    config.setTenantSchemaProvider(schemaProvider);

    config.setDdlRun(false);
    config.setDatabasePlatform(new MySqlPlatform());

    final Database database = DatabaseFactory.create(config);
    database.shutdown();
  }

  /**
   *  Tests using multi tenancy per schema
   */
  @Test
  public void create_new_server_with_multi_tenancy_catalog() {

    String tenant = "customer";
    CurrentTenantProvider tenantProvider = Mockito.mock(CurrentTenantProvider.class);
    Mockito.doReturn(tenant).when(tenantProvider).currentId();

    TenantCatalogProvider catalogProvider = Mockito.mock(TenantCatalogProvider.class);
    Mockito.doReturn("tenant_catalog").when(catalogProvider).catalog(tenant);

    DatabaseBuilder config = new DatabaseConfig();
    config.setName("h2");
    config.loadFromProperties();
    config.setName("multi-tenancy");
    config.setRegister(false);
    config.setDefaultServer(false);

    config.setTenantMode(TenantMode.CATALOG);
    config.setCurrentTenantProvider(tenantProvider);
    config.setTenantCatalogProvider(catalogProvider);

    config.setDdlRun(false);
    config.setDatabasePlatform(new MySqlPlatform());

    final Database database = DatabaseFactory.create(config);
    database.shutdown();
  }
}
