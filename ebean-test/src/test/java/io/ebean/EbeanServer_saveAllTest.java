package io.ebean;

import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class EbeanServer_saveAllTest extends BaseTestCase {

  @Test
  public void saveAll() {

    List<EBasicVer> someBeans = beans(3);

    // act
    LoggedSql.start();
    DB.saveAll(someBeans);

    // assert
    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(4);
    assertThat(loggedSql.get(0)).contains("insert into e_basicver (");
    assertThat(loggedSql.get(0)).contains("name, description, other, last_update) values (");

    for (EBasicVer someBean : someBeans) {
      someBean.setName(someBean.getName() + "-mod");
    }

    // act
    LoggedSql.start();
    DB.updateAll(someBeans);

    loggedSql = LoggedSql.stop();

    assertThat(loggedSql).hasSize(3);
    assertThat(loggedSql.get(0)).contains("update e_basicver set name=?, last_update=? where id=? ");

    // act
    LoggedSql.start();
    DB.deleteAll(someBeans);

    loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(4);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id=?");
  }

  @Test
  public void saveAll_withExistingBatch_doesNotTriggerFlush() {

    Database server = DB.getDefault();

    try (Transaction transaction = server.beginTransaction()) {
      transaction.setBatchMode(true);
      LoggedSql.start();

      for (EBasicVer bean : beans(2)) {
        server.save(bean);
      }

      // jdbc batch, no sql yet
      assertThat(LoggedSql.collect()).isEmpty();

      server.saveAll(beans(3));

      // still batch, no sql yet
      assertThat(LoggedSql.collect()).isEmpty();

      for (EBasicVer bean : beans(2)) {
        server.save(bean);
      }
      // still batch, no sql yet
      assertThat(LoggedSql.collect()).isEmpty();

      // flush now
      transaction.commit();

      // and we have our SQL from jdbc batch flush
      assertThat(LoggedSql.stop()).isNotEmpty();
    }

  }

  @Test
  public void deleteAll_withNull() {
    DB.deleteAll(null);
  }

  @Test
  public void deleteAll_withEmpty() {
    DB.saveAll(beans(0));
  }

  @Test
  public void saveAll_withNull() {
    DB.saveAll((Collection<?>)null);
  }

  @Test
  public void saveAll_withEmpty() {
    DB.saveAll(beans(0));
  }

  @Test
  public void saveAll_withTransaction() {

    List<EBasicVer> someBeans = beans(3);
    Database server = DB.getDefault();

    // act
    LoggedSql.start();
    try (Transaction txn = server.beginTransaction()) {
      server.saveAll(someBeans, txn);
      txn.commit();
    }

    // assert
    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(4);
    assertThat(loggedSql.get(0)).contains("insert into e_basicver (");
    assertThat(loggedSql.get(0)).contains("name, description, other, last_update) values (");

    for (EBasicVer someBean : someBeans) {
      someBean.setName(someBean.getName() + "-mod");
    }

    // act
    LoggedSql.start();

    try (Transaction txn = server.beginTransaction()) {
      server.updateAll(someBeans, txn);
      txn.commit();
    }
    loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(3);
    assertThat(loggedSql.get(0)).contains("update e_basicver set name=?, last_update=? where id=? ");

    // act
    LoggedSql.start();
    try (Transaction txn = server.beginTransaction()) {
      server.deleteAll(someBeans, txn);
      txn.commit();
    }
    loggedSql = LoggedSql.stop();
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
