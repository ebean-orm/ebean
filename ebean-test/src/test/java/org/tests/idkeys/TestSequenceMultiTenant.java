package org.tests.idkeys;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.multitenant.partition.UserContext;
import org.tests.idkeys.db.GenKeySequence;

import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import io.ebean.config.TenantDataSourceProvider;
import io.ebean.config.TenantMode;
import io.ebean.config.dbplatform.h2.H2Platform;

/**
 * Tests sequences for multiple tenants.
 */
public class TestSequenceMultiTenant {

  /**
   * Tests sequences using multi tenancy per database
   */
  @Test
  public void test_multi_tenant_sequences() {

    EbeanServer db = init();

    UserContext.set("4711", "1");
    assertEquals(1L, db.nextId(GenKeySequence.class));
    assertEquals(2L, db.nextId(GenKeySequence.class));

    UserContext.set("5711", "2");
    assertEquals(1L, db.nextId(GenKeySequence.class));

    UserContext.set("4711", "1");
    assertEquals(3L, db.nextId(GenKeySequence.class));

  }

  private static EbeanServer init() {

    ServerConfig config = new ServerConfig();

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
        return map.computeIfAbsent(tenantId,
            TestSequenceMultiTenant::createDataSource);
      }
    });

    config.getClasses().add(GenKeySequence.class);

    return EbeanServerFactory.create(config);
  }

  private static DataSource createDataSource(Object tenantId) {

    ServerConfig config = new ServerConfig();

    config.setName("h2multitenantseq");
    config.loadFromProperties();
    config.setDdlRun(true);
    config.setDdlExtra(false);
    config.setRegister(false);
    config.setDefaultServer(false);
    config.getDataSourceConfig()
        .setUrl("jdbc:h2:mem:h2multitenantseq-" + tenantId);

    EbeanServer server = EbeanServerFactory.create(config);

    return server.getPluginApi().getDataSource();
  }

}