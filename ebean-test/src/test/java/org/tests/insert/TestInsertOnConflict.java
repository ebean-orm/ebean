package org.tests.insert;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.InsertOptions;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.ForPlatform;
import org.junit.jupiter.api.Test;
import org.tests.update.EPersonOnline;

import java.util.List;

import static io.ebean.InsertOptions.ON_CONFLICT_NOTHING;
import static io.ebean.InsertOptions.ON_CONFLICT_UPDATE;
import static org.assertj.core.api.Assertions.assertThat;

class TestInsertOnConflict extends BaseTestCase {

  InsertOptions onConflictDoUpdateAndGetGeneratedKeys = InsertOptions.builder()
    .onConflictUpdate()
    .getGeneratedKeys(true)
    .build();

  @ForPlatform({Platform.POSTGRES, Platform.YUGABYTE})
  @Test
  void insertOnConflictUpdate_when_noIdValue() {
    Database db = DB.getDefault();
    db.truncate(EPersonOnline.class);
    LoggedSql.start();

    var bean = newBean("a@b.com");
    db.insert(bean, onConflictDoUpdateAndGetGeneratedKeys);
    assertThat(bean.getId()).isNotNull();

    var bean2 = newBean("a@b.com");
    bean2.setOnlineStatus(false);
    db.insert(bean2, onConflictDoUpdateAndGetGeneratedKeys);
    assertThat(bean2.getId()).isEqualTo(bean.getId());

    var sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("insert into e_person_online (email, online_status, when_updated) values (?,?,?) on conflict (email) do update set online_status=excluded.online_status, when_updated=excluded.when_updated");
    assertThat(sql.get(1)).contains("insert into e_person_online (email, online_status, when_updated) values (?,?,?) on conflict (email) do update set online_status=excluded.online_status, when_updated=excluded.when_updated");

    List<EPersonOnline> list = db.find(EPersonOnline.class).findList();

    assertThat(list).hasSize(1);
    assertThat(list.get(0).getWhenUpdated()).isEqualTo(bean2.getWhenUpdated());
  }

  @ForPlatform({Platform.POSTGRES, Platform.YUGABYTE})
  @Test
  void insertOnConflictUpdate_when_idValueSupplied() {
    Database db = DB.getDefault();
    db.truncate(EPersonOnline.class);
    LoggedSql.start();

    var bean = newBean("a@b.com");
    bean.setId(40_042L);
    db.insert(bean, ON_CONFLICT_UPDATE);
    assertThat(bean.getId()).isNotNull();

    var bean2 = newBean("a@b.com");
    bean2.setId(40_043L); // not expected but can be different
    bean2.setOnlineStatus(false);

    db.insert(bean2, onConflictDoUpdateAndGetGeneratedKeys);

    var sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("insert into e_person_online (id, email, online_status, when_updated) values (?,?,?,?) on conflict (email) do update set online_status=excluded.online_status, when_updated=excluded.when_updated");
    assertThat(sql.get(1)).contains("insert into e_person_online (id, email, online_status, when_updated) values (?,?,?,?) on conflict (email) do update set online_status=excluded.online_status, when_updated=excluded.when_updated");

    List<EPersonOnline> list = db.find(EPersonOnline.class).findList();

    assertThat(list).hasSize(1);
    assertThat(list.get(0).getWhenUpdated()).isEqualTo(bean2.getWhenUpdated());
  }

  @ForPlatform({Platform.POSTGRES, Platform.YUGABYTE})
  @Test
  void insertAll_onConflictUpdate_when_noIdValue() {
    Database db = DB.getDefault();
    db.truncate(EPersonOnline.class);
    LoggedSql.start();

    var bean = newBean("a1@b.com");
    var bean2 = newBean("a2@b.com");
    var bean3 = newBean("a3@b.com");
    db.insertAll(List.of(bean, bean2, bean3), ON_CONFLICT_UPDATE);

    var sql = LoggedSql.stop();
    assertThat(sql).hasSize(5);
    assertThat(sql.get(0)).contains("insert into e_person_online (email, online_status, when_updated) values (?,?,?) on conflict (email) do update set online_status=excluded.online_status, when_updated=excluded.when_updated");
    assertThat(sql.get(1)).contains(" -- bind");
    assertThat(sql.get(2)).contains(" -- bind");
    assertThat(sql.get(3)).contains(" -- bind");
    assertThat(sql.get(4)).contains(" -- executeBatch()");

    var bean4 = newBean("a1@b.com");
    var bean5 = newBean("a5@b.com");
    db.insertAll(List.of(bean4, bean5), ON_CONFLICT_UPDATE);

    List<EPersonOnline> list = db.find(EPersonOnline.class).orderBy("id").findList();
    assertThat(list).hasSize(4);
  }

  @ForPlatform({Platform.POSTGRES, Platform.YUGABYTE})
  @Test
  void explicitConstraint() {
    Database db = DB.getDefault();
    db.truncate(EPersonOnline.class);
    LoggedSql.start();

    var bean = newBean("a1@b.com");
    var bean2 = newBean("a2@b.com");
    var bean3 = newBean("a3@b.com");

    var ON_CONFLICT_ = InsertOptions.builder()
      .onConflictUpdate()
      .constraint("uq_e_person_online_email")
      .build();

    db.insertAll(List.of(bean, bean2, bean3), ON_CONFLICT_);

    var sql = LoggedSql.stop();
    assertThat(sql).hasSize(5);
    assertThat(sql.get(0)).contains("insert into e_person_online (email, online_status, when_updated) values (?,?,?) on conflict on constraint uq_e_person_online_email do update set online_status=excluded.online_status, when_updated=excluded.when_updated");
    assertThat(sql.get(1)).contains(" -- bind");
    assertThat(sql.get(2)).contains(" -- bind");
    assertThat(sql.get(3)).contains(" -- bind");
    assertThat(sql.get(4)).contains(" -- executeBatch()");

    List<EPersonOnline> list = db.find(EPersonOnline.class).orderBy("id").findList();
    assertThat(list).hasSize(3);
  }

  @ForPlatform({Platform.POSTGRES, Platform.YUGABYTE})
  @Test
  void explicitUniqueColumns() {
    Database db = DB.getDefault();
    db.truncate(EPersonOnline.class);
    LoggedSql.start();

    var bean = newBean("a1@b.com");
    var bean2 = newBean("a2@b.com");
    var bean3 = newBean("a3@b.com");

    var ON_CONFLICT_ = InsertOptions.builder()
      .onConflictUpdate()
      .uniqueColumns("  email  ")
      .build();

    db.insertAll(List.of(bean, bean2, bean3), ON_CONFLICT_);

    var sql = LoggedSql.stop();
    assertThat(sql).hasSize(5);
    assertThat(sql.get(0)).contains("insert into e_person_online (email, online_status, when_updated) values (?,?,?) on conflict (  email  ) do update set online_status=excluded.online_status, when_updated=excluded.when_updated");
    assertThat(sql.get(1)).contains(" -- bind");
    assertThat(sql.get(2)).contains(" -- bind");
    assertThat(sql.get(3)).contains(" -- bind");
    assertThat(sql.get(4)).contains(" -- executeBatch()");

    List<EPersonOnline> list = db.find(EPersonOnline.class).orderBy("id").findList();
    assertThat(list).hasSize(3);
  }

  @ForPlatform({Platform.POSTGRES, Platform.YUGABYTE})
  @Test
  void explicitUpdateSet() {
    Database db = DB.getDefault();
    db.truncate(EPersonOnline.class);
    LoggedSql.start();

    var bean = newBean("a1@b.com");
    var bean2 = newBean("a2@b.com");

    var ON_CONFLICT_ = InsertOptions.builder()
      .onConflictUpdate()
      .updateSet("when_updated=excluded.when_updated, online_status=true")
      .build();

    db.insertAll(List.of(bean, bean2), ON_CONFLICT_);

    var sql = LoggedSql.stop();
    assertThat(sql).hasSize(4);
    assertThat(sql.get(0)).contains("insert into e_person_online (email, online_status, when_updated) values (?,?,?) on conflict (email) do update set when_updated=excluded.when_updated, online_status=true");
    assertThat(sql.get(1)).contains(" -- bind");
    assertThat(sql.get(2)).contains(" -- bind");
    assertThat(sql.get(3)).contains(" -- executeBatch()");

    List<EPersonOnline> list = db.find(EPersonOnline.class).orderBy("id").findList();
    assertThat(list).hasSize(2);
  }

  @ForPlatform({Platform.POSTGRES, Platform.YUGABYTE})
  @Test
  void insertOnConflictNothing_when_noIdValue() {
    Database db = DB.getDefault();
    db.truncate(EPersonOnline.class);
    LoggedSql.start();

    var bean = newBean("a@b.com");
    db.insert(bean, ON_CONFLICT_NOTHING);
    assertThat(bean.getId()).isNotNull();

    var bean2 = newBean("a@b.com");
    bean2.setOnlineStatus(false);
    db.insert(bean2, ON_CONFLICT_NOTHING);
    assertThat(bean2.getId()).isNull();

    var sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("insert into e_person_online (email, online_status, when_updated) values (?,?,?) on conflict (email) do nothing");
    assertThat(sql.get(1)).contains("insert into e_person_online (email, online_status, when_updated) values (?,?,?) on conflict (email) do nothing");

    List<EPersonOnline> list = db.find(EPersonOnline.class).findList();

    assertThat(list).hasSize(1);
    assertThat(list.get(0).getWhenUpdated()).isEqualTo(bean.getWhenUpdated());
  }

  private static EPersonOnline newBean(String email) {
    EPersonOnline bean = new EPersonOnline();
    bean.setEmail(email);
    bean.setOnlineStatus(true);
    return bean;
  }
}
