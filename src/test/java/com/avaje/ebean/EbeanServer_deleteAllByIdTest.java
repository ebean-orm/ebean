package com.avaje.ebean;

import com.avaje.tests.model.basic.EBasicVer;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class EbeanServer_deleteAllByIdTest {

  @Test
  public void deleteAllById() {

    List<EBasicVer> someBeans = beans(3);

    Ebean.saveAll(someBeans);
    List<Integer> ids = new ArrayList<Integer>();
    for (EBasicVer someBean : someBeans) {
      ids.add(someBean.getId());
    }

    // act
    LoggedSqlCollector.start();
    Ebean.deleteAll(EBasicVer.class, ids);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id  in (?,?,?)");
  }

  @Test
  public void deleteAllById_withTransaction() {

    List<EBasicVer> someBeans = beans(3);

    Ebean.saveAll(someBeans);
    List<Integer> ids = new ArrayList<Integer>();
    for (EBasicVer someBean : someBeans) {
      ids.add(someBean.getId());
    }

    EbeanServer server = Ebean.getDefaultServer();
    // act
    LoggedSqlCollector.start();
    Transaction txn = server.beginTransaction();
    try {
      server.deleteAll(EBasicVer.class, ids, txn);
      txn.commit();
    } finally {
      txn.end();
    }
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id  in (?,?,?)");
  }

  @Test
  public void deleteAllPermanentById() {

    List<EBasicVer> someBeans = beans(3);

    Ebean.saveAll(someBeans);
    List<Integer> ids = new ArrayList<Integer>();
    for (EBasicVer someBean : someBeans) {
      ids.add(someBean.getId());
    }

    LoggedSqlCollector.start();

    Ebean.deleteAllPermanent(EBasicVer.class, ids);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id  in (?,?,?)");
  }


  @Test
  public void deleteAllPermanentById_withTransaction() {

    List<EBasicVer> someBeans = beans(3);

    Ebean.saveAll(someBeans);
    List<Integer> ids = new ArrayList<Integer>();
    for (EBasicVer someBean : someBeans) {
      ids.add(someBean.getId());
    }

    EbeanServer server = Ebean.getDefaultServer();
    // act
    LoggedSqlCollector.start();
    Transaction txn = server.beginTransaction();
    try {
      server.deleteAllPermanent(EBasicVer.class, ids, txn);
      txn.commit();
    } finally {
      txn.end();
    }
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id  in (?,?,?)");
  }

  private List<EBasicVer> beans(int count) {
    List<EBasicVer> beans = new ArrayList<EBasicVer>();
    for (int i = 0; i < count; i++) {
      beans.add(bean("foo" + i));
    }
    return beans;
  }

  private EBasicVer bean(String name) {
    EBasicVer bean = new EBasicVer();
    bean.setName(name);
    return bean;
  }
}