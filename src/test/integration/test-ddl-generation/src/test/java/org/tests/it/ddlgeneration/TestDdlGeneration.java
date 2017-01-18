package org.tests.it.ddlgeneration;


import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.Platform;
import io.ebean.config.PropertyMap;
import io.ebean.config.ServerConfig;
import io.ebean.config.TenantMode;
import io.ebean.dbmigration.DbMigration;

public class TestDdlGeneration {
  
  protected EbeanServer server() {
    return Ebean.getDefaultServer();
  }

  @Test
  public void test() throws IOException {
AtomicInteger tenantProvider = new AtomicInteger();

    ServerConfig config = new ServerConfig();
    config.loadFromProperties();
    config.setCurrentUserProvider(() -> "default"); 
    config.setDefaultServer(true);
    config.setRegister(true);

    config.setTenantMode(TenantMode.SCHEMA);
    config.setCurrentTenantProvider(tenantProvider::get);
    config.setTenantSchemaProvider(tenantId -> "tenant_" + tenantId);
    
    EbeanServerFactory.create(config);

    DbMigration dbMigration = new DbMigration();
    dbMigration.addPlatform(Platform.POSTGRES, "postgres");
    dbMigration.addPlatform(Platform.SQLSERVER, "sqlserver");
    dbMigration.generateMigration();
  }
}
