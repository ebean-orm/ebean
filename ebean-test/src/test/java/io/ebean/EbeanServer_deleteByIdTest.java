package io.ebean;

import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class EbeanServer_deleteByIdTest extends BaseTestCase {

  @Test
  public void deleteById() {

    EBasicVer someBean = bean("foo1");
    DB.save(someBean);

    // act
    LoggedSql.start();
    DB.delete(EBasicVer.class, someBean.getId());

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id = ?");
  }

  @Test
  public void deletePermanentById() {

    EBasicVer someBean = bean("foo1");
    DB.save(someBean);

    // act
    LoggedSql.start();
    DB.deletePermanent(EBasicVer.class, someBean.getId());

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id = ?");
  }


  @Test
  public void deleteById_withTransaction() {

    EBasicVer someBean = bean("foo1");
    DB.save(someBean);

    Database server = DB.getDefault();
    // act
    LoggedSql.start();
    Transaction txn = server.beginTransaction();
    try {
      server.delete(EBasicVer.class, someBean.getId(), txn);
      txn.commit();
    } finally {
      txn.end();
    }
    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id = ?");
  }

  @Test
  public void deletePermanentById_withTransaction() {

    EBasicVer someBean = bean("foo2");
    DB.save(someBean);

    Database server = DB.getDefault();
    // act
    LoggedSql.start();
    Transaction txn = server.beginTransaction();
    try {
      server.deletePermanent(EBasicVer.class, someBean.getId(), txn);
      txn.commit();
    } finally {
      txn.end();
    }
    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from e_basicver where id = ?");
  }

  private EBasicVer bean(String name) {
    return new EBasicVer(name);
  }
}
