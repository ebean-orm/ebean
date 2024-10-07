package org.tests.persistencecontext;

import io.ebean.*;
import io.ebean.config.ContainerConfig;
import io.ebean.DatabaseBuilder;
import io.ebean.config.DatabaseConfig;
import io.ebean.xtest.BaseTestCase;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPersistenceContextServerConfig extends BaseTestCase {

  @Test
  public void test_config() {
    SpiEbeanServer ebeanServer = (SpiEbeanServer) create();
    try {
      Query<EBasicVer> query = ebeanServer.find(EBasicVer.class);
      PersistenceContextScope scope = ebeanServer.persistenceContextScope((SpiQuery<?>) query);

      assertEquals(PersistenceContextScope.QUERY, scope);
    } finally {
      ebeanServer.shutdown();
    }
  }

  static Database create() {

    DatabaseBuilder config = new DatabaseConfig();
    config.setName("withPCQuery");
    config.setDdlExtra(false);

    Properties properties = new Properties();
    properties.setProperty("datasource.withPCQuery.username", "sa");
    properties.setProperty("datasource.withPCQuery.password", "");
    properties.setProperty("datasource.withPCQuery.databaseUrl", "jdbc:h2:mem:withPCQuery;");
    properties.setProperty("datasource.withPCQuery.databaseDriver", "org.h2.Driver");

    config.loadFromProperties(properties);
    config.setPersistenceContextScope(PersistenceContextScope.QUERY);
    config.setContainerConfig(new ContainerConfig());
    config.setDefaultServer(false);
    config.setRegister(false);

    config.addClass(EBasicVer.class);
    return DatabaseFactory.create(config);
  }
}
