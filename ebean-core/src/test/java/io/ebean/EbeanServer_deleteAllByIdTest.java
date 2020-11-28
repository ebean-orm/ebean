package io.ebean;

import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.EBasicVer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;


public class EbeanServer_deleteAllByIdTest extends BaseTestCase {

  @Test
  public void saveAllByVarArgs() {

    final EBasicVer bean0 = bean("foo0");
    final EBasicVer bean1 = bean("foo1");
    final EBasicVer bean2 = bean("foo2");

    LoggedSqlCollector.start();

    DB.saveAll(bean0, bean1, bean2);

    assertNotNull(bean0.getId());
    assertNotNull(bean1.getId());
    assertNotNull(bean2.getId());

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(4);
    assertThat(loggedSql.get(0)).contains("insert into e_basicver");
    assertSqlBind(loggedSql, 1, 3);

    List<Integer> ids = new ArrayList<>();
    ids.add(bean0.getId());
    ids.add(bean1.getId());
    ids.add(bean2.getId());
    DB.deleteAll(EBasicVer.class, ids);
  }

  @Test
  public void deleteAllById() {

    List<EBasicVer> someBeans = beans(3);

    DB.saveAll(someBeans);
    List<Integer> ids = new ArrayList<>();
    for (EBasicVer someBean : someBeans) {
      ids.add(someBean.getId());
    }

    // act
    LoggedSqlCollector.start();
    DB.deleteAll(EBasicVer.class, ids);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    platformAssertIn(loggedSql.get(0), "delete from e_basicver where id ");
  }

  @Test
  public void deleteAllById_withTransaction() {

    List<EBasicVer> someBeans = beans(3);

    DB.saveAll(someBeans);
    List<Integer> ids = new ArrayList<>();
    for (EBasicVer someBean : someBeans) {
      ids.add(someBean.getId());
    }

    Database db = DB.getDefault();
    // act
    LoggedSqlCollector.start();
    try (Transaction txn = db.beginTransaction()) {
      db.deleteAll(EBasicVer.class, ids, txn);
      txn.commit();
    }
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    platformAssertIn(loggedSql.get(0), "delete from e_basicver where id ");
  }

  @Test
  public void deleteAllPermanentById() {

    List<EBasicVer> someBeans = beans(3);

    DB.saveAll(someBeans);
    List<Integer> ids = new ArrayList<>();
    for (EBasicVer someBean : someBeans) {
      ids.add(someBean.getId());
    }

    LoggedSqlCollector.start();

    DB.deleteAllPermanent(EBasicVer.class, ids);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    platformAssertIn(loggedSql.get(0), "delete from e_basicver where id ");
  }


  @Test
  public void deleteAllPermanentById_withTransaction() {

    List<EBasicVer> someBeans = beans(3);

    DB.saveAll(someBeans);
    List<Integer> ids = new ArrayList<>();
    for (EBasicVer someBean : someBeans) {
      ids.add(someBean.getId());
    }

    Database db = DB.getDefault();
    // act
    LoggedSqlCollector.start();
    try (Transaction txn = db.beginTransaction()) {
      db.deleteAllPermanent(EBasicVer.class, ids, txn);
      txn.commit();
    }
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    platformAssertIn(loggedSql.get(0), "delete from e_basicver where id ");
  }

  private List<EBasicVer> beans(int count) {
    List<EBasicVer> beans = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      beans.add(bean("foo" + i));
    }
    return beans;
  }

  private EBasicVer bean(String name) {
    return new EBasicVer(name);
  }
}
