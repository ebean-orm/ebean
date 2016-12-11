package org.tests.persistencecontext;

import io.ebean.BaseTestCase;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.PersistenceContextScope;
import io.ebean.Query;
import io.ebean.config.ContainerConfig;
import io.ebean.config.ServerConfig;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import org.tests.model.basic.EBasicVer;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TestPersistenceContextServerConfig extends BaseTestCase {

  @Test
  public void test_config() {

    SpiEbeanServer ebeanServer = (SpiEbeanServer) create();

    Query<EBasicVer> query = ebeanServer.find(EBasicVer.class);

    PersistenceContextScope scope = ebeanServer.getPersistenceContextScope((SpiQuery<?>) query);

    assertEquals(PersistenceContextScope.QUERY, scope);
  }

  static EbeanServer create() {

    System.setProperty("ebean.ignoreExtraDdl", "true");

    ServerConfig config = new ServerConfig();
    config.setName("withPCQuery");

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

//    DataSourceConfig dataSourceConfig = new DataSourceConfig();
//    dataSourceConfig.setUsername("sa");
//    dataSourceConfig.setPassword("");
//    dataSourceConfig.setUrl("jdbc:h2:mem:withPCQuery;");
//    dataSourceConfig.setDriver("org.h2.Driver");
//    config.setDataSourceConfig(dataSourceConfig);

    config.addClass(EBasicVer.class);

    return EbeanServerFactory.create(config);
  }
}
