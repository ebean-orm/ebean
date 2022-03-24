package org.tests.cache;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.BackgroundExecutorWrapper;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.MdcBackgroundExecutorWrapper;
import io.ebeaninternal.server.cache.DefaultServerCachePlugin;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.OCachedBean;

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class testing async/background cache updates in a multi-tenant environment.
 */
public class TestBeanCacheAsync extends BaseTestCase {

  private final ThreadLocal<String> tenantId = new ThreadLocal<>();

  class ThreadLocalTenantProvider implements CurrentTenantProvider {

    @Override
    public Object currentId() {
      return tenantId.get();
    }
  }

  /**
   * Copy tenant info to the background thread.
   */
  class TenantCopyBackgroundExecutorWrapper implements BackgroundExecutorWrapper {
    @Override
    public <T> Callable<T> wrap(Callable<T> task) {
      String tenant = tenantId.get(); // executed in current thread
      return () -> {
        tenantId.set(tenant);  // executed in other thread
        try {
          return task.call();
        } finally {
          tenantId.remove();
        }
      };
    }

    @Override
    public Runnable wrap(Runnable task) {
      String tenant = tenantId.get();
      return () -> {
        tenantId.set(tenant);
        try {
          task.run();
        } finally {
          tenantId.remove();
        }
      };
    }
  }

  @Test
  public void findById_with_tenant() throws InterruptedException {
    DatabaseConfig config = new DatabaseConfig();
    config.setName(DB.getDefault().name());
    config.loadFromProperties();
    config.setDataSource(DB.getDefault().dataSource());
    config.setReadOnlyDataSource(DB.getDefault().readOnlyDataSource());
    config.setDdlExtra(false);
    config.setDdlGenerate(false);
    config.setDdlRun(false);
    config.setDefaultServer(false);
    config.setRegister(false);
    config.setServerCachePlugin(new DefaultServerCachePlugin()); // disables foreground local caching (as it is done in Hz/Ignite)
    config.setCurrentTenantProvider(new ThreadLocalTenantProvider());
    config.setBackgroundExecutorWrapper(
        new MdcBackgroundExecutorWrapper().with(new TenantCopyBackgroundExecutorWrapper()));
    tenantId.set("4711");

    Database db = DatabaseFactory.create(config);
    try {
      OCachedBean bean = new OCachedBean();
      bean.setName("findById");
      db.save(bean);

      OCachedBean bean0 = db.find(OCachedBean.class, bean.getId());
      assertNotNull(bean0);
      assertThat(bean0.getName()).isEqualTo("findById");
      bean0.setName("findById2");
      db.save(bean0);

      Thread.sleep(100); // TODO: can we block finds on that ID if a pending cache update is present?

      bean0 = db.find(OCachedBean.class, bean.getId());
      assertNotNull(bean0);
      assertThat(bean0.getName()).isEqualTo("findById2");

    } finally {
      db.shutdown(false, false);
    }
  }

}
