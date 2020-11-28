package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.FetchGroup;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static io.ebean.CacheMode.ON;
import static org.assertj.core.api.Assertions.assertThat;

public class TestBeanFetchJoinCache extends BaseTestCase {

  private final ServerCache customerBeanCache = server().getServerCacheManager().getBeanCache(Customer.class);

  @Test
  public void fetchCache_when_allHits() {

    initDataClearCache();
    loadCustomerBeanCache();

    customerBeanCache.getStatistics(true);

    LoggedSqlCollector.start();

    List<Order> orders = DB.find(Order.class)
      .fetchCache("customer", "status, name")
      .findList();

    final List<String> sql0 = LoggedSqlCollector.current();
    assertThat(sql0).hasSize(1);

    final ServerCacheStatistics statistics = customerBeanCache.getStatistics(true);
    assertThat(statistics.getHitCount()).isEqualTo(2);

    assertThat(trimSql(sql0.get(0))).doesNotContain("t1.status");

    for (Order order : orders) {
      final Customer customer = order.getCustomer();
      assertThat(customer.getName()).isNotNull();
      assertThat(customer.getStatus()).isNotNull();
    }

    assertThat(LoggedSqlCollector.stop()).isEmpty();
  }

  private void loadCustomerBeanCache() {
    DB.find(Customer.class)
      .setBeanCacheMode(ON)
      .findList();
  }

  @Test
  public void fetchCache_when_someHits() {

    initDataClearCache();

    DB.find(Customer.class)
      .setBeanCacheMode(ON)
      .where().lt("id", 2)
      .findList();

    customerBeanCache.getStatistics(true);

    LoggedSqlCollector.start();

    List<Order> orders = DB.find(Order.class)
      .fetchCache("customer")
      .findList();

    final List<String> sql0 = LoggedSqlCollector.current();
    assertThat(sql0).hasSize(2);
    assertThat(sql0.get(0)).contains(" from o_order ");
    assertThat(sql0.get(1)).contains(" from o_customer t0 where t0.id ");

    final ServerCacheStatistics statistics = customerBeanCache.getStatistics(true);
    assertThat(statistics.getHitCount()).isEqualTo(1);

    for (Order order : orders) {
      final Customer customer = order.getCustomer();
      assertThat(customer.getName()).isNotNull();
      assertThat(customer.getStatus()).isNotNull();
    }

    assertThat(LoggedSqlCollector.stop()).isEmpty();
  }

  @Test
  public void fetchCache_when_hitsButBeanCachePartiallyLoaded() {

    initDataClearCache();

    DB.find(Customer.class)
      .setBeanCacheMode(ON)
      .select("name") // not included status in bean cache
      .findList();

    customerBeanCache.getStatistics(true);

    LoggedSqlCollector.start();

    List<Order> orders = DB.find(Order.class)
      .fetchCache("customer")
      .findList();

    final List<String> sql0 = LoggedSqlCollector.current();
    assertThat(sql0).hasSize(1);

    final ServerCacheStatistics statistics = customerBeanCache.getStatistics(true);
    assertThat(statistics.getHitCount()).isEqualTo(2);

    assertThat(trimSql(sql0.get(0))).doesNotContain("t1.status");

    for (Order order : orders) {
      final Customer customer = order.getCustomer();
      assertThat(customer.getName()).isNotNull();
      assertThat(customer.getStatus()).isNotNull(); // We hit the DB here as previously hit bean cache
    }

    // assert we didn't hit the L2 bean cache the second time around
    final ServerCacheStatistics statistics1 = customerBeanCache.getStatistics(true);
    assertThat(statistics1.getHitCount()).isEqualTo(0);

    // assert we did hit the DB the second time around
    final List<String> sql1 = LoggedSqlCollector.stop();
    assertThat(sql1).hasSize(1);
    assertThat(sql1.get(0)).contains(" from o_customer t0 ");
  }

  private final FetchGroup<Order> fgBasic = FetchGroup.of(Order.class)
    .fetchCache("customer")
    .build();

  @Test
  public void fetchGroup_fetchCache() {

    initDataClearCache();

    loadCustomerBeanCache();
    customerBeanCache.getStatistics(true);

    List<Order> orders = DB.find(Order.class)
      .select(fgBasic)
      .findList();

    assertThat(orders).isNotEmpty();

    final ServerCacheStatistics statistics1 = customerBeanCache.getStatistics(true);
    assertThat(statistics1.getHitCount()).isEqualTo(2);
  }

  private final FetchGroup<Order> fgCachePartial = FetchGroup.of(Order.class)
    .fetchCache("customer", "name")
    .build();


  @Test
  public void fetchGroup_fetchCache_partial() {

    initDataClearCache();

    customerBeanCache.getStatistics(true);

    LoggedSqlCollector.start();

    List<Order> orders = DB.find(Order.class)
      .select(fgCachePartial)
      .findList();

    assertThat(orders).isNotEmpty();

    final ServerCacheStatistics statistics1 = customerBeanCache.getStatistics(true);
    assertThat(statistics1.getMissCount()).isEqualTo(2);

    final List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains(" from o_order ");
    assertSql(sql.get(1)).contains(" from o_customer ");
  }

  private void initDataClearCache() {
    ResetBasicData.reset();
    server().getServerCacheManager().clearAll();
  }
}
