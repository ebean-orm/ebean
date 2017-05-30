package io.ebean;

import org.tests.model.basic.EBasicVer;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class EbeanServer_deleteTest extends BaseTestCase  {

  @Test
  public void delete() {

    EBasicVer someBean = bean("foo1");
    Ebean.save(someBean);

    // act
    LoggedSqlCollector.start();
    Ebean.delete(someBean);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id=? and ");
  }

  @Test
  public void delete_withTransaction() {

    EBasicVer someBean = bean("foo1");
    Ebean.save(someBean);

    EbeanServer server = Ebean.getDefaultServer();
    // act
    LoggedSqlCollector.start();
    Transaction txn = server.beginTransaction();
    try {
      server.delete(someBean, txn);
      txn.commit();
    } finally {
      txn.end();
    }
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id=? and ");
  }

  private EBasicVer bean(String name) {
    return new EBasicVer(name);
  }
}
