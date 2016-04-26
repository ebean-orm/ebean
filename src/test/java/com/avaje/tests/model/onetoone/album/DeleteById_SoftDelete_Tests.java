package com.avaje.tests.model.onetoone.album;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteById_SoftDelete_Tests {

  @Test
  public void ebean_deleteById_when_softDelete() {

    Cover cover = new Cover("a1");
    cover.save();

    LoggedSqlCollector.start();

    Ebean.delete(Cover.class, cover.getId());

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update cover set deleted=true where id = ?");

    cover.deletePermanent();
  }

  @Test
  public void deleteById_when_softDelete() {

    Cover cover = new Cover("a1");
    cover.save();

    LoggedSqlCollector.start();

    Ebean.getDefaultServer().delete(Cover.class, cover.getId(), null);

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update cover set deleted=true where id = ?");

    cover.deletePermanent();
  }

  @Test
  public void deletePermanentById_when_softDelete() {

    Cover cover = new Cover("a2");
    cover.save();

    LoggedSqlCollector.start();

    Ebean.getDefaultServer().deletePermanent(Cover.class, cover.getId(), null);

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("delete from cover where id = ?");
  }

  @Test
  public void deleteAllById_when_softDelete() {

    EbeanServer server = Ebean.getDefaultServer();
    List<Cover> beans = beans(2);
    server.saveAll(beans);

    LoggedSqlCollector.start();

    server.deleteAll(Cover.class, ids(beans));

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update cover set deleted=true where id  in (?,?)");
  }

  @Test
  public void deleteAllById_when_softDelete_withTransaction() {

    EbeanServer server = Ebean.getDefaultServer();
    List<Cover> beans = beans(2);
    server.saveAll(beans);

    LoggedSqlCollector.start();

    Transaction transaction = server.beginTransaction();
    try {
      server.deleteAll(Cover.class, ids(beans), transaction);
      server.commitTransaction();
    } finally {
      server.endTransaction();
    }

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update cover set deleted=true where id  in (?,?)");
  }

  @Test
  public void ebean_deleteAllPermanentById_when_softDelete() {

    List<Cover> beans = beans(2);
    Ebean.saveAll(beans);

    LoggedSqlCollector.start();

    Ebean.deleteAllPermanent(Cover.class, ids(beans));

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("delete from cover where id  in (?,?)");
  }

  @Test
  public void deleteAllPermanentById_when_softDelete() {

    EbeanServer server = Ebean.getDefaultServer();
    List<Cover> beans = beans(2);
    server.saveAll(beans);

    LoggedSqlCollector.start();

    server.deleteAllPermanent(Cover.class, ids(beans));

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("delete from cover where id  in (?,?)");
  }


  @Test
  public void deleteAllPermanentById_when_softDelete_withTransaction() {

    EbeanServer server = Ebean.getDefaultServer();
    List<Cover> beans = beans(2);
    server.saveAll(beans);

    LoggedSqlCollector.start();

    Transaction transaction = server.beginTransaction();
    try {
      server.deleteAllPermanent(Cover.class, ids(beans), transaction);
      server.commitTransaction();
    } finally {
      server.endTransaction();
    }

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("delete from cover where id  in (?,?)");
  }

  @Test
  public void ebean_deleteAll_when_softDelete() {

    List<Cover> beans = beans(2);
    Ebean.saveAll(beans);

    LoggedSqlCollector.start();

    Ebean.deleteAll(beans);

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("update cover set s3url=?, deleted=? where id=?");
  }

  @Test
  public void deleteAll_when_softDelete() {

    EbeanServer server = Ebean.getDefaultServer();
    List<Cover> beans = beans(2);
    server.saveAll(beans);

    LoggedSqlCollector.start();

    server.deleteAll(beans);

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("update cover set s3url=?, deleted=? where id=?");
  }

  @Test
  public void deleteAll_when_softDelete_withTransaction() {

    EbeanServer server = Ebean.getDefaultServer();
    List<Cover> beans = beans(2);
    server.saveAll(beans);

    LoggedSqlCollector.start();

    Transaction transaction = server.beginTransaction();
    try {
      server.deleteAll(beans, transaction);
      server.commitTransaction();
    } finally {
      server.endTransaction();
    }

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("update cover set s3url=?, deleted=? where id=?");
  }

  @Test
  public void deleteAllPermanent_when_softDelete() {

    EbeanServer server = Ebean.getDefaultServer();
    List<Cover> beans = beans(2);
    server.saveAll(beans);

    LoggedSqlCollector.start();

    server.deleteAllPermanent(beans);

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from cover where id=?");
  }

  @Test
  public void deleteAllPermanent_when_softDelete_withTransaction() {

    EbeanServer server = Ebean.getDefaultServer();
    List<Cover> beans = beans(2);
    server.saveAll(beans);

    LoggedSqlCollector.start();

    Transaction transaction = server.beginTransaction();
    try {
      server.deleteAllPermanent(beans, transaction);
      server.commitTransaction();
    } finally {
      server.endTransaction();
    }

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from cover where id=?");
  }

  private List<Long> ids(List<Cover> beans) {
    List<Long> ids = new ArrayList<Long>();
    for (Cover someBean : beans) {
      ids.add(someBean.getId());
    }
    return ids;
  }

  private List<Cover> beans(int count) {
    List<Cover> beans = new ArrayList<Cover>();
    for (int i = 0; i < count; i++) {
      beans.add(new Cover("delById " + i));
    }
    return beans;
  }
}
