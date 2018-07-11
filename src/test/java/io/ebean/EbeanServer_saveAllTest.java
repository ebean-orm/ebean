package io.ebean;

import io.ebean.annotation.PersistBatch;
import org.tests.model.basic.EBasicVer;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

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
    for (String insertSql : loggedSql) {
      assertThat(insertSql).contains("insert into e_basicver (");
      assertThat(insertSql).contains("name, description, other, last_update) values (");
    }

    for (EBasicVer someBean : someBeans) {
      someBean.setName(someBean.getName() + "-mod");
    }

    // act
    LoggedSqlCollector.start();
    Ebean.updateAll(someBeans);

    loggedSql = LoggedSqlCollector.stop();
    for (String updateSql : loggedSql) {
      assertThat(updateSql).contains("update e_basicver set name=?, last_update=? where id=? ");
    }


    // act
    LoggedSqlCollector.start();
    Ebean.deleteAll(someBeans);

    loggedSql = LoggedSqlCollector.stop();
    for (String updateSql : loggedSql) {
      assertThat(updateSql).contains("delete from e_basicver where id=? ");
    }

  }

  @Test
  public void saveAll_withExistingBatch_doesNotTriggerFlush() {

    EbeanServer server = Ebean.getDefaultServer();

    Transaction transaction = server.beginTransaction();
    try {
      transaction.setBatch(PersistBatch.ALL);
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

    } finally {
      transaction.end();
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
    Transaction txn = server.beginTransaction();
    try {
      server.saveAll(someBeans, txn);
      txn.commit();
    } finally {
      txn.end();
    }

    // assert
    List<String> loggedSql = LoggedSqlCollector.stop();
    for (String insertSql : loggedSql) {
      assertThat(insertSql).contains("insert into e_basicver (");
      assertThat(insertSql).contains("name, description, other, last_update) values (");
    }

    for (EBasicVer someBean : someBeans) {
      someBean.setName(someBean.getName() + "-mod");
    }

    // act
    LoggedSqlCollector.start();
    txn = server.beginTransaction();
    try {
      server.updateAll(someBeans, txn);
      txn.commit();
    } finally {
      txn.end();
    }
    loggedSql = LoggedSqlCollector.stop();
    for (String updateSql : loggedSql) {
      assertThat(updateSql).contains("update e_basicver set name=?, last_update=? where id=? ");
    }


    // act
    LoggedSqlCollector.start();
    txn = server.beginTransaction();
    try {
      server.deleteAll(someBeans, txn);
      txn.commit();
    } finally {
      txn.end();
    }
    loggedSql = LoggedSqlCollector.stop();
    for (String updateSql : loggedSql) {
      assertThat(updateSql).contains("delete from e_basicver where id=? ");
    }

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
