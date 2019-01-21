package io.ebean;

import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.EBasicVer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class EbeanServer_saveAllTest extends BaseTestCase {

  @Test
  public void saveAll() {

    List<EBasicVer> someBeans = beans(3);

    // act
    LoggedSqlCollector.start();
    Ebean.saveAll(someBeans);

    // assert
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(4);
    assertThat(loggedSql.get(0)).contains("insert into e_basicver (");
    assertThat(loggedSql.get(0)).contains("name, description, other, last_update) values (");

    for (EBasicVer someBean : someBeans) {
      someBean.setName(someBean.getName() + "-mod");
    }

    // act
    LoggedSqlCollector.start();
    Ebean.updateAll(someBeans);

    loggedSql = LoggedSqlCollector.stop();

    assertThat(loggedSql).hasSize(3);
    assertThat(loggedSql.get(0)).contains("update e_basicver set name=?, last_update=? where id=? ");

    // act
    LoggedSqlCollector.start();
    Ebean.deleteAll(someBeans);

    loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(4);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id=?");
  }

  @Test
  public void saveAll_withExistingBatch_doesNotTriggerFlush() {

    EbeanServer server = Ebean.getDefaultServer();

    try (Transaction transaction = server.beginTransaction()) {
      transaction.setBatchMode(true);
      LoggedSqlCollector.start();

      for (EBasicVer bean : beans(2)) {
        server.save(bean);
      }

      // jdbc batch, no sql yet
      assertThat(LoggedSqlCollector.current()).isEmpty();

      server.saveAll(beans(3));

      // still batch, no sql yet
      assertThat(LoggedSqlCollector.current()).isEmpty();

      for (EBasicVer bean : beans(2)) {
        server.save(bean);
      }
      // still batch, no sql yet
      assertThat(LoggedSqlCollector.current()).isEmpty();

      // flush now
      transaction.commit();

      // and we have our SQL from jdbc batch flush
      assertThat(LoggedSqlCollector.stop()).isNotEmpty();
    }

  }

  @Test
  public void deleteAll_withNull() {
    Ebean.deleteAll(null);
  }

  @Test
  public void deleteAll_withEmpty() {
    Ebean.saveAll(beans(0));
  }

  @Test
  public void saveAll_withNull() {
    Ebean.saveAll(null);
  }

  @Test
  public void saveAll_withEmpty() {
    Ebean.saveAll(beans(0));
  }

  @Test
  public void saveAll_withTransaction() {

    List<EBasicVer> someBeans = beans(3);
    EbeanServer server = Ebean.getDefaultServer();

    // act
    LoggedSqlCollector.start();
    try (Transaction txn = server.beginTransaction()) {
      server.saveAll(someBeans, txn);
      txn.commit();
    }

    // assert
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(4);
    assertThat(loggedSql.get(0)).contains("insert into e_basicver (");
    assertThat(loggedSql.get(0)).contains("name, description, other, last_update) values (");

    for (EBasicVer someBean : someBeans) {
      someBean.setName(someBean.getName() + "-mod");
    }

    // act
    LoggedSqlCollector.start();

    try (Transaction txn = server.beginTransaction()) {
      server.updateAll(someBeans, txn);
      txn.commit();
    }
    loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(3);
    assertThat(loggedSql.get(0)).contains("update e_basicver set name=?, last_update=? where id=? ");

    // act
    LoggedSqlCollector.start();
    try (Transaction txn = server.beginTransaction()) {
      server.deleteAll(someBeans, txn);
      txn.commit();
    }
    loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(4);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id=?");
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
