package io.ebean.xtest.base;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Transaction;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class EbeanServer_deleteTest extends BaseTestCase {

  @Test
  public void delete() {

    EBasicVer someBean = bean("foo1");
    DB.save(someBean);

    // act
    LoggedSql.start();
    DB.delete(someBean);

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id=? and ");
  }

  @Test
  public void delete_withTransaction() {

    EBasicVer someBean = bean("foo1");
    DB.save(someBean);

    Database server = DB.getDefault();
    // act
    LoggedSql.start();
    Transaction txn = server.beginTransaction();
    try {
      server.delete(someBean, txn);
      txn.commit();
    } finally {
      txn.end();
    }
    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id=? and ");
  }

  private EBasicVer bean(String name) {
    return new EBasicVer(name);
  }
}
