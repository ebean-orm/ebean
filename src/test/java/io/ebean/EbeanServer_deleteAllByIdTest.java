package io.ebean;

import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.EBasicVer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class EbeanServer_deleteAllByIdTest extends BaseTestCase {

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
