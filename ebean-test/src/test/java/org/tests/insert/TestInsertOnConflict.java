package org.tests.insert;

import io.ebean.*;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.ForPlatform;
import org.junit.jupiter.api.Test;
import org.tests.update.EPersonOnline;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
  void insertTestEntity() {
    var entity1 = new EStrIdBean();
    entity1.setId("entity-1");
    entity1.setName("Example");
    DB.insert(entity1);

    var entity2 = new EStrIdBean();
    entity2.setId("entity-1");
    entity2.setName("Example");

    DB.getDefault().insert(entity2, InsertOptions.builder()
      .onConflictNothing()
      .uniqueColumns("id")
      .build());
  }

  @ForPlatform({Platform.POSTGRES, Platform.YUGABYTE})
  @Test
  void insertOnConflictUpdateExplicitTransaction() {
    Database db = DB.getDefault();
    db.truncate(EPersonOnline.class);
    LoggedSql.start();

    var bean = newBean("a@b.com");

    try (Transaction txn = DB.createTransaction()) {
      db.insert(bean, ON_CONFLICT_UPDATE, txn);
      txn.commit();
    }
    assertThat(bean.getId()).isNotNull();

    var bean2 = newBean("a@b.com");
    bean2.setOnlineStatus(false);
    try (Transaction txn = DB.createTransaction()) {
      db.insert(bean2, ON_CONFLICT_UPDATE, txn);
      txn.commit();
    }
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
  void insertAll_onConflictUpdate_explicitTransaction() {
    Database db = DB.getDefault();
    db.truncate(EPersonOnline.class);
    LoggedSql.start();

    try (Transaction txn = DB.createTransaction()) {
      txn.setBatchSize(3);
      var bean = newBean("a1@b.com");
      var bean2 = newBean("a2@b.com");
      var bean3 = newBean("a3@b.com");
      var bean4 = newBean("a4@b.com");
      db.insertAll(List.of(bean, bean2, bean3, bean4), ON_CONFLICT_UPDATE, txn);
      txn.commit();
    }

    var sql = LoggedSql.stop();
    assertThat(sql).hasSize(8);
    assertThat(sql.get(0)).contains("insert into e_person_online (email, online_status, when_updated) values (?,?,?) on conflict (email) do update set online_status=excluded.online_status, when_updated=excluded.when_updated");
    assertThat(sql.get(1)).contains(" -- bind");
    assertThat(sql.get(2)).contains(" -- bind");
    assertThat(sql.get(3)).contains(" -- bind");
    assertThat(sql.get(4)).contains(" -- executeBatch()");
    assertThat(sql.get(5)).contains("insert into e_person_online (email, online_status, when_updated) values (?,?,?) on conflict (email) do update set online_status=excluded.online_status, when_updated=excluded.when_updated");
    assertThat(sql.get(6)).contains(" -- bind");
    assertThat(sql.get(7)).contains(" -- executeBatch()");

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

  @ForPlatform({Platform.POSTGRES, Platform.YUGABYTE})
  @Test
  void updateQueryReturning() throws SQLException {
    Database db = DB.getDefault();
    db.truncate(EPersonOnline.class);

    var bean1 = newBean("a1@bee.com");
    var bean2 = newBean("a2@cee.com");
    var bean3 = newBean("a3@bee.com");
    db.saveAll(bean1, bean2, bean3);

    String sql = "update e_person_online set email = concat('x',email) where email like ? returning id, email, online_status";

    try (Transaction txn = db.createTransaction()) {
      Connection connection = txn.connection();
      try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setString(1, "%bee.com");
        try (ResultSet resultSet = pstmt.executeQuery()) {
          while (resultSet.next()) {
            long id = resultSet.getLong(1);
            String email = resultSet.getString(2);
            boolean status = resultSet.getBoolean(3);
            // do something with the id, email and status
            assertThat(id).isGreaterThan(0);
            assertThat(email).startsWith("x");
            assertThat(status).isTrue();
          }
        }
      }
      txn.commit();
    }

    try (Transaction txn = db.beginTransaction()) {
      List<SqlRow> sqlRowList = DB.sqlQuery(sql)
        .setParameter("%bee.com")
        .findList();

      assertThat(sqlRowList).hasSize(2);
      assertThat(sqlRowList.get(0).getString("email")).startsWith("xx");

      txn.addModification("e_person_online", false, true, false);
      txn.commit();
    }

    try (Transaction txn = db.beginTransaction()) {
      List<ReturnDto> dtoList = db.findDto(ReturnDto.class, sql)
        .setParameter("%bee.com")
        .findList();

      assertThat(dtoList).hasSize(2);
      assertThat(dtoList.get(0).email).startsWith("xxx");

      txn.addModification("e_person_online", false, true, false);
      txn.commit();
    }

    try (Transaction txn = db.beginTransaction()) {
      List<ReturnDto2> dtoList2 = db.findDto(ReturnDto2.class, sql)
        .setParameter("%bee.com")
        .findList();

      assertThat(dtoList2).hasSize(2);
      assertThat(dtoList2.get(0).email).startsWith("xxxx");
      assertThat(dtoList2.get(0).id).isGreaterThan(0);
      txn.addModification("e_person_online", false, true, false);
      txn.commit();
    }
  }

  public static class ReturnDto {

    private final long id;
    private final String email;
    private final boolean onlineStatus;

      public ReturnDto(long id, String email, boolean onlineStatus) {
          this.id = id;
          this.email = email;
          this.onlineStatus = onlineStatus;
      }
  }

  public static class ReturnDto2 {

    private long id;
    private String email;
    private boolean onlineStatus;

    public long getId() {
      return id;
    }

    public void setId(long id) {
      this.id = id;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public boolean isOnlineStatus() {
      return onlineStatus;
    }

    public void setOnlineStatus(boolean onlineStatus) {
      this.onlineStatus = onlineStatus;
    }
  }

  private static EPersonOnline newBean(String email) {
    EPersonOnline bean = new EPersonOnline();
    bean.setEmail(email);
    bean.setOnlineStatus(true);
    return bean;
  }
}
