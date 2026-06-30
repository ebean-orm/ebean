package org.tests.idkeys;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.TenantDataSourceProvider;
import io.ebean.config.TenantMode;
import io.ebean.platform.h2.H2Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.multitenant.partition.UserContext;
import org.tests.idkeys.db.GenKeySeqA;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sequence id generation must be tenant aware - each tenant (DB per tenant) has its
 * own sequence so allocation does not bleed across tenants.
 */
class TenantSequenceTest {

  @AfterEach
  void after() {
    UserContext.reset();
  }

  @Test
  void dbPerTenant_sequencesAreIndependent() {
    Database db = setup();
    try {
      UserContext.set("u1", "1");
      assertThat(db.nextId(GenKeySeqA.class)).isEqualTo(1L);
      assertThat(db.nextId(GenKeySeqA.class)).isEqualTo(2L);

      // tenant 2 uses its own database/sequence - starts fresh at 1
      UserContext.set("u2", "2");
      assertThat(db.nextId(GenKeySeqA.class)).isEqualTo(1L);

      // tenant 1 continues from where it left off
      UserContext.set("u1", "1");
      assertThat(db.nextId(GenKeySeqA.class)).isEqualTo(3L);
    } finally {
      db.shutdown();
    }
  }

  private Database setup() {
    DatabaseConfig config = new DatabaseConfig();
    config.setName("tenantSeq");
    config.setRegister(false);
    config.setDefaultServer(false);
    config.setDatabasePlatform(new H2Platform());
    config.setTenantMode(TenantMode.DB);
    config.setCurrentTenantProvider(() -> UserContext.get().getTenantId());
    config.setTenantDataSourceProvider(new TenantDataSourceProvider() {
      final Map<Object, DataSource> map = new ConcurrentHashMap<>();

      @Override
      public DataSource dataSource(Object tenantId) {
        if (tenantId == null) {
          tenantId = "1";
        }
        return map.computeIfAbsent(tenantId, this::create);
      }

      private DataSource create(Object tenantId) {
        DatabaseConfig c = new DatabaseConfig();
        c.setName("tenantSeq-" + tenantId);
        c.setRegister(false);
        c.setDefaultServer(false);
        c.setDdlGenerate(true);
        c.setDdlRun(true);
        c.setDdlExtra(false);
        c.getDataSourceConfig().setUrl("jdbc:h2:mem:tenantSeq-" + tenantId + ";DB_CLOSE_DELAY=-1");
        c.getDataSourceConfig().setUsername("sa");
        c.getDataSourceConfig().setPassword("");
        c.getClasses().add(GenKeySeqA.class);
        return DatabaseFactory.create(c).dataSource();
      }
    });
    config.getClasses().add(GenKeySeqA.class);
    return DatabaseFactory.create(config);
  }
}
