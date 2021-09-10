package org.tests.model.onetoone.album;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DeleteById_SoftDelete_Tests extends BaseTestCase {

  @Test
  public void ebean_deleteById_when_softDelete() {

    Cover cover = new Cover("a1");
    cover.save();

    LoggedSql.start();

    DB.delete(Cover.class, cover.getId());

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    if (isPlatformBooleanNative()) {
      assertSql(sql.get(0)).contains("update cover set deleted=true where id = ?");
    } else {
      assertSql(sql.get(0)).contains("update cover set deleted=1 where id = ?");
    }

    cover.deletePermanent();
  }

  @Test
  public void undo_softDelete() {

    Cover cover = new Cover("b1");
    cover.save();

    DB.delete(Cover.class, cover.getId());

    Cover findWhenSoft = DB.find(Cover.class, cover.getId());
    assertNull(findWhenSoft);

    Cover cover1 = DB.find(Cover.class)
      .setIncludeSoftDeletes()
      .setId(cover.getId())
      .findOne();

    cover1.setDeleted(false);

    LoggedSql.start();

    cover1.update();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("update cover set deleted=? where id=?; -- bind(false");

    Cover findAgain = DB.find(Cover.class, cover.getId());
    assertNotNull(findAgain);

    cover.deletePermanent();
  }

  @Test
  public void deleteById_when_softDelete() {

    Cover cover = new Cover("a1");
    cover.save();

    LoggedSql.start();

    DB.getDefault().delete(Cover.class, cover.getId(), null);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    if (isPlatformBooleanNative()) {
      assertSql(sql.get(0)).contains("update cover set deleted=true where id = ?");
    } else {
      assertSql(sql.get(0)).contains("update cover set deleted=1 where id = ?");
    }
    cover.deletePermanent();
  }

  @Test
  public void deletePermanentById_when_softDelete() {

    Cover cover = new Cover("a2");
    cover.save();

    LoggedSql.start();

    DB.getDefault().deletePermanent(Cover.class, cover.getId(), null);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("delete from cover where id = ?");
  }

  @Test
  public void deleteAllById_when_softDelete() {

    List<Cover> beans = beans(2);
    DB.saveAll(beans);

    LoggedSql.start();

    DB.deleteAll(Cover.class, ids(beans));

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    if (isPlatformBooleanNative()) {
      assertSql(sql.get(0)).contains("update cover set deleted=true where id ");
    } else {
      assertSql(sql.get(0)).contains("update cover set deleted=1 where id ");
    }
  }

  @Test
  public void deleteAllById_when_softDelete_withTransaction() {

    List<Cover> beans = beans(2);
    DB.saveAll(beans);

    LoggedSql.start();

    try (Transaction transaction = DB.beginTransaction()) {
      DB.getDefault().deleteAll(Cover.class, ids(beans), transaction);
      transaction.commit();
    }

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    if (isPlatformBooleanNative()) {
      assertSql(sql.get(0)).contains("update cover set deleted=true where id");
    } else {
      assertSql(sql.get(0)).contains("update cover set deleted=1 where id");
    }
  }

  @Test
  public void ebean_deleteAllPermanentById_when_softDelete() {

    List<Cover> beans = beans(2);
    DB.saveAll(beans);

    LoggedSql.start();

    DB.deleteAllPermanent(Cover.class, ids(beans));

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    platformAssertIn(sql.get(0), "delete from cover where id ");
  }

  @Test
  public void deleteAllPermanentById_when_softDelete() {

    List<Cover> beans = beans(2);
    DB.saveAll(beans);

    LoggedSql.start();

    DB.deleteAllPermanent(Cover.class, ids(beans));

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    platformAssertIn(sql.get(0), "delete from cover where id ");
  }


  @Test
  public void deleteAllPermanentById_when_softDelete_withTransaction() {

    List<Cover> beans = beans(2);
    DB.saveAll(beans);

    LoggedSql.start();

    try (Transaction transaction = DB.beginTransaction()) {
      DB.getDefault().deleteAllPermanent(Cover.class, ids(beans), transaction);
      transaction.commit();
    }

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    platformAssertIn(sql.get(0), "delete from cover where id ");
  }

  @Test
  public void ebean_deleteAll_when_softDelete() {

    List<Cover> beans = beans(2);
    DB.saveAll(beans);

    LoggedSql.start();

    DB.deleteAll(beans);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(3);
    if (isPersistBatchOnCascade()) {
      assertSql(sql.get(0)).contains("update cover set deleted=? where id=?");
    } else {
      assertSql(sql.get(0)).contains("update cover set deleted=? where id=?");
    }
  }

  @Test
  public void deleteAll_when_softDelete() {

    List<Cover> beans = beans(2);
    DB.saveAll(beans);

    LoggedSql.start();

    DB.deleteAll(beans);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(3);
    if (isPersistBatchOnCascade()) {
      assertSql(sql.get(0)).contains("update cover set deleted=? where id=?");
    }
    else {
      assertSql(sql.get(0)).contains("update cover set deleted=? where id=?");
    }
  }

  @Test
  public void deleteAll_when_softDelete_withTransaction() {

    List<Cover> beans = beans(2);
    DB.saveAll(beans);

    LoggedSql.start();

    try (Transaction transaction = DB.beginTransaction()) {
      DB.getDefault().deleteAll(beans, transaction);
      transaction.commit();
    }

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(3);
    if (isPersistBatchOnCascade()) {
      assertSql(sql.get(0)).contains("update cover set deleted=? where id=?");
    }
    else {
      assertSql(sql.get(0)).contains("update cover set deleted=? where id=?");
    }
  }

  @Test
  public void deleteAllPermanent_when_softDelete() {

    List<Cover> beans = beans(2);
    DB.saveAll(beans);

    LoggedSql.start();

    DB.deleteAllPermanent(beans);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(3);
    assertSql(sql.get(0)).contains("delete from cover where id=?");
  }

  @Test
  public void deleteAllPermanent_when_softDelete_withTransaction() {

    List<Cover> beans = beans(2);
    DB.saveAll(beans);

    LoggedSql.start();

    try (Transaction transaction = DB.beginTransaction()) {
      DB.getDefault().deleteAllPermanent(beans, transaction);
      transaction.commit();
    }

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(3);
    assertSql(sql.get(0)).contains("delete from cover where id=?");
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
