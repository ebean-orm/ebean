package org.tests.model.onetoone.album;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DeleteById_SoftDelete_Tests extends BaseTestCase {

  @Test
  public void ebean_deleteById_when_softDelete() {

    Cover cover = new Cover("a1");
    cover.save();

    LoggedSqlCollector.start();

    Ebean.delete(Cover.class, cover.getId());

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    if (isPlatformBooleanNative()) {
      assertThat(sql.get(0)).contains("update cover set deleted=true where id = ?");
    } else {
      assertThat(sql.get(0)).contains("update cover set deleted=1 where id = ?");
    }

    cover.deletePermanent();
  }

  @Test
  public void undo_softDelete() {

    Cover cover = new Cover("b1");
    cover.save();

    Ebean.delete(Cover.class, cover.getId());

    Cover findWhenSoft = Ebean.find(Cover.class, cover.getId());
    assertNull(findWhenSoft);

    Cover cover1 = Ebean.find(Cover.class)
      .setIncludeSoftDeletes()
      .setId(cover.getId())
      .findOne();

    cover1.setDeleted(false);

    LoggedSqlCollector.start();

    cover1.update();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("update cover set deleted=? where id=?; --bind(false");

    Cover findAgain = Ebean.find(Cover.class, cover.getId());
    assertNotNull(findAgain);

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
    if (isPlatformBooleanNative()) {
      assertThat(sql.get(0)).contains("update cover set deleted=true where id = ?");
    } else {
      assertThat(sql.get(0)).contains("update cover set deleted=1 where id = ?");
    }
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
    if (isPlatformBooleanNative()) {
      assertThat(sql.get(0)).contains("update cover set deleted=true where id ");
    } else {
      assertThat(sql.get(0)).contains("update cover set deleted=1 where id ");
    }
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
    if (isPlatformBooleanNative()) {
      assertThat(sql.get(0)).contains("update cover set deleted=true where id");
    } else {
      assertThat(sql.get(0)).contains("update cover set deleted=1 where id");
    }
  }

  @Test
  public void ebean_deleteAllPermanentById_when_softDelete() {

    List<Cover> beans = beans(2);
    Ebean.saveAll(beans);

    LoggedSqlCollector.start();

    Ebean.deleteAllPermanent(Cover.class, ids(beans));

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    platformAssertIn(sql.get(0), "delete from cover where id ");
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
    platformAssertIn(sql.get(0), "delete from cover where id ");
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
    platformAssertIn(sql.get(0), "delete from cover where id ");
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
    List<Long> ids = new ArrayList<>();
    for (Cover someBean : beans) {
      ids.add(someBean.getId());
    }
    return ids;
  }

  private List<Cover> beans(int count) {
    List<Cover> beans = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      beans.add(new Cover("delById " + i));
    }
    return beans;
  }
}
