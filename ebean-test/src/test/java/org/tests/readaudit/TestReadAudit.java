package org.tests.readaudit;

import io.ebean.DatabaseBuilder;
import io.ebean.xtest.BaseTestCase;
import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.FutureList;
import io.ebean.cache.ServerCache;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.config.DatabaseConfig;
import io.ebean.event.readaudit.ReadAuditLogger;
import io.ebean.event.readaudit.ReadAuditPrepare;
import io.ebean.event.readaudit.ReadAuditQueryPlan;
import io.ebean.event.readaudit.ReadEvent;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.EBasicChangeLog;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class TestReadAudit extends BaseTestCase {

  TDReadAuditPrepare readAuditPrepare = new TDReadAuditPrepare();

  TDReadAuditLogger readAuditLogger = new TDReadAuditLogger();

  Database server;

  Long id1;
  Long id2;

  @BeforeEach
  public void setup() {
    server = createServer();

    EBasicChangeLog bean = new EBasicChangeLog();
    bean.setName("readAudito1");
    bean.setShortDescription("readAudit hello");
    server.save(bean);
    id1 = bean.getId();

    EBasicChangeLog bean2 = new EBasicChangeLog();
    bean2.setName("readAudito2");
    bean2.setShortDescription("readAudit hi");
    server.save(bean2);
    id2 = bean2.getId();

    Country ar = new Country();
    ar.setCode("AR");
    ar.setName("Argentina");
    server.save(ar);
  }

  @AfterEach
  public void shutdown() {
    server.shutdown();
  }

  @Test
  public void test_findById() {
    resetCounters();

    EBasicChangeLog found = server.find(EBasicChangeLog.class, id1);
    assertThat(found).isNotNull();
    assertThat(readAuditPrepare.count).isEqualTo(1);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.beans).hasSize(1);
    assertThat(readAuditLogger.beans.get(0).getBeanType()).isEqualTo(EBasicChangeLog.class.getName());
    assertThat(readAuditLogger.beans.get(0).getId()).isEqualTo(id1);
  }

  @Test
  public void test_findById_usingL2Cache() {
    resetCounters();

    ServerCache beanCache = server.cacheManager().beanCache(EBasicChangeLog.class);
    beanCache.statistics(true);

    EBasicChangeLog found = server.find(EBasicChangeLog.class, id1);
    assertThat(found).isNotNull();
    assertThat(readAuditPrepare.count).isEqualTo(1);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.beans).hasSize(1);
    assertThat(readAuditLogger.beans.get(0).getBeanType()).isEqualTo(EBasicChangeLog.class.getName());
    assertThat(readAuditLogger.beans.get(0).getId()).isEqualTo(id1);

    ServerCacheStatistics statistics = beanCache.statistics(false);
    assertThat(statistics.getSize()).isEqualTo(1);
    assertThat(statistics.getHitCount()).isEqualTo(0);

    EBasicChangeLog found2 = server.find(EBasicChangeLog.class, id1);
    assertThat(found2).isNotNull();
    statistics = beanCache.statistics(false);
    assertThat(statistics.getSize()).isEqualTo(1);
    assertThat(statistics.getHitCount()).isEqualTo(1);

    assertThat(readAuditLogger.beans).hasSize(2);
  }

  @Test
  public void test_findById_usingL2Cache_sharedBean() {
    resetCounters();

    ServerCache beanCache = server.cacheManager().beanCache(Country.class);
    beanCache.statistics(true);

    Country found = server.find(Country.class, "AR");
    assertThat(found).isNotNull();
    assertThat(readAuditPrepare.count).isEqualTo(1);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.beans).hasSize(1);
    assertThat(readAuditLogger.beans.get(0).getBeanType()).isEqualTo(Country.class.getName());
    assertThat(readAuditLogger.beans.get(0).getId()).isEqualTo("AR");

    ServerCacheStatistics statistics = beanCache.statistics(false);
    assertThat(statistics.getSize()).isEqualTo(1);
    assertThat(statistics.getHitCount()).isEqualTo(0);

    Country found2 = server.find(Country.class, "AR");
    assertThat(found2).isNotNull();
    statistics = beanCache.statistics(false);
    assertThat(statistics.getSize()).isEqualTo(1);
    assertThat(statistics.getHitCount()).isEqualTo(1);

    assertThat(readAuditLogger.beans).hasSize(2);

    Country ref = server.reference(Country.class, "AR");
    assertThat(readAuditLogger.beans).hasSize(2);
    assertThat(ref).isNotSameAs(found2);
  }

  @Test
  public void test_findList() {

    resetCounters();

    List<EBasicChangeLog> list = server.find(EBasicChangeLog.class)
      .where().startsWith("shortDescription", "readAudit")
      .findList();

    assertThat(list).hasSize(2);
    assertThat(readAuditPrepare.count).isEqualTo(1);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.many).hasSize(1);
    assertThat(readAuditLogger.many.get(0).getBeanType()).isEqualTo(EBasicChangeLog.class.getName());
    assertThat(readAuditLogger.many.get(0).getIds()).contains(id1, id2);

    server.find(EBasicChangeLog.class)
      .where().startsWith("shortDescription", "readAudit")
      .findList();

    assertThat(readAuditPrepare.count).isEqualTo(2);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.many).hasSize(2);
  }

  @Test
  public void test_findList_useL2Cache() {

    resetCounters();

    LoggedSql.start();

    List<EBasicChangeLog> list = server.find(EBasicChangeLog.class)
      .setUseQueryCache(true)
      .where().startsWith("shortDescription", "readAudit")
      .findList();

    System.out.println("test_findList_useL2Cache> first query");

    assertThat(list).hasSize(2);
    assertThat(readAuditPrepare.count).isEqualTo(1);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.many).hasSize(1);
    assertThat(readAuditLogger.many.get(0).getBeanType()).isEqualTo(EBasicChangeLog.class.getName());
    assertThat(readAuditLogger.many.get(0).getIds()).contains(id1, id2);

    List<EBasicChangeLog> list1 = server.find(EBasicChangeLog.class)
      .setUseQueryCache(true)
      .where().startsWith("shortDescription", "readAudit")
      .findList();

    System.out.println("test_findList_useL2Cache> second query " + list1.size());
    assertThat(list1).hasSize(2);
    List<String> sql = LoggedSql.stop();
    System.out.println("test_findList_useL2Cache> sql: " + sql);
    //assertThat(sql).hasSize(1);

    System.out.println("test_findList_useL2Cache> prepare:" + readAuditPrepare.count
      + " plans:" + readAuditLogger.plans
      + " many:" + readAuditLogger.many);

    assertThat(readAuditPrepare.count).isEqualTo(1);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.many).hasSize(1);
  }

  @Test
  public void test_findFutureList() throws ExecutionException, InterruptedException {

    resetCounters();

    FutureList<EBasicChangeLog> futureList = server.find(EBasicChangeLog.class)
      .where().startsWith("shortDescription", "readAudit")
      .findFutureList();

    List<EBasicChangeLog> list = futureList.get();
    assertThat(list).hasSize(2);
    assertThat(readAuditPrepare.count).isEqualTo(1);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.many).hasSize(1);
    assertThat(readAuditLogger.many.get(0).getBeanType()).isEqualTo(EBasicChangeLog.class.getName());
    assertThat(readAuditLogger.many.get(0).getIds()).contains(id1, id2);

    server.find(EBasicChangeLog.class)
      .where().startsWith("shortDescription", "readAudit")
      .findList();

    assertThat(readAuditPrepare.count).isEqualTo(2);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.many).hasSize(2);
  }

  @Test
  public void test_findSet() {

    resetCounters();

    Set<EBasicChangeLog> list = server.find(EBasicChangeLog.class)
      .where().startsWith("shortDescription", "readAudit")
      .findSet();

    assertThat(list).hasSize(2);
    assertThat(readAuditPrepare.count).isEqualTo(1);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.many).hasSize(1);
    assertThat(readAuditLogger.many.get(0).getIds()).contains(id1, id2);

    server.find(EBasicChangeLog.class)
      .where().startsWith("shortDescription", "readAudit")
      .findSet();

    assertThat(readAuditPrepare.count).isEqualTo(2);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.many).hasSize(2);
  }

  @Test
  public void test_findMap() {

    resetCounters();

    Map<Long, EBasicChangeLog> list = server.find(EBasicChangeLog.class)
      .where().startsWith("shortDescription", "readAudit")
      .findMap();

    assertThat(list).hasSize(2);
    assertThat(readAuditPrepare.count).isEqualTo(1);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.many).hasSize(1);
    assertThat(readAuditLogger.many.get(0).getIds()).contains(id1, id2);

    server.find(EBasicChangeLog.class)
      .where().startsWith("shortDescription", "readAudit")
      .findMap();

    assertThat(readAuditPrepare.count).isEqualTo(2);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.many).hasSize(2);
  }

  @Test
  public void test_findEach() {

    resetCounters();

    final AtomicInteger count = new AtomicInteger();
    server.find(EBasicChangeLog.class)
      .where().startsWith("shortDescription", "readAudit")
      .findEach(bean -> count.incrementAndGet());

    assertThat(count.get()).isEqualTo(2);
    assertThat(readAuditPrepare.count).isEqualTo(1);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.many).hasSize(1);
    assertThat(readAuditLogger.many.get(0).getIds()).contains(id1, id2);

    server.find(EBasicChangeLog.class)
      .where().startsWith("shortDescription", "readAudit")
      .findEach(bean -> count.incrementAndGet());

    assertThat(readAuditPrepare.count).isEqualTo(2);
    assertThat(readAuditLogger.plans).hasSize(1);
    assertThat(readAuditLogger.many).hasSize(2);
  }

  private Database createServer() {

    DatabaseBuilder config = new DatabaseConfig();
    config.setName("h2other");
    config.loadFromProperties();

    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDdlExtra(false);

    config.setDefaultServer(false);
    config.setRegister(false);

    config.addClass(Country.class);
    config.addClass(EBasicChangeLog.class);

    config.setReadAuditLogger(readAuditLogger);
    config.setReadAuditPrepare(readAuditPrepare);

    return DatabaseFactory.create(config);
  }

  private void resetCounters() {
    readAuditLogger.resetCounters();
    readAuditPrepare.resetCounters();
  }

  static class TDReadAuditPrepare implements ReadAuditPrepare {

    int count;

    void resetCounters() {
      count = 0;
    }

    @Override
    public void prepare(ReadEvent event) {
      count++;
      event.setUserId("appUser1");
      event.setUserIpAddress("1.1.1.1");
      event.getUserContext().put("some", "thing");
    }
  }

  static class TDReadAuditLogger implements ReadAuditLogger {

    Set<ReadAuditQueryPlan> plans = new HashSet<>();
    List<ReadEvent> beans = new ArrayList<>();
    List<ReadEvent> many = new ArrayList<>();

    void resetCounters() {
      plans.clear();
      beans.clear();
      many.clear();
    }

    @Override
    public void queryPlan(ReadAuditQueryPlan queryPlan) {
      plans.add(queryPlan);
    }

    @Override
    public void auditBean(ReadEvent readBean) {
      beans.add(readBean);
    }

    @Override
    public void auditMany(ReadEvent readMany) {
      many.add(readMany);
    }
  }

}
