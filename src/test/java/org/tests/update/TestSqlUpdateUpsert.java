package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSqlUpdateUpsert extends BaseTestCase {

  @ForPlatform(Platform.H2)
  @Test
  public void h2Merge() {

    String sql = "merge into e_person_online (email, online, when_updated) key(email) values (?, ?, now())";

    String email = "baz@one.com";

    Object key = Ebean.createSqlUpdate(sql)
      .setGetGeneratedKeys(true)
      .setParameter(1, email)
      .setParameter(2, true)
      .executeGetKey();

    EPersonOnline found = Ebean.find(EPersonOnline.class, key);
    assertThat(found).isNotNull();
    assertThat(found.getEmail()).isEqualTo(email);
    assertThat(found.isOnline()).isTrue();

    String sqlNamed = "merge into e_person_online (email, online, when_updated) key(email) values (:email, :online, now())";

    SqlUpdate sqlUpdate2 = Ebean.createSqlUpdate(sqlNamed)
      .setGetGeneratedKeys(true)
      .setParameter("email", email)
      .setParameter("online", false);

    Object key2 = sqlUpdate2.executeGetKey();
    assertThat(key2).isNull();


    EPersonOnline found2 = Ebean.find(EPersonOnline.class).where().eq("email", email).findOne();
    assertThat(found2).isNotNull();
    assertThat(found2.getId()).isEqualTo(key);
    assertThat(found2.getEmail()).isEqualTo(email);
    assertThat(found2.isOnline()).isFalse();
    assertThat(found2.getWhenUpdated()).isGreaterThan(found.getWhenUpdated());
  }

  @ForPlatform(Platform.POSTGRES)
  @Test
  public void postgresUpsert() {

    String sql = "insert into e_person_online (email, online, when_updated) values (?, ?, now()) on conflict (email) do update set when_updated=now(), online = ?";

    String email = "foo@one.com";

    Object key = Ebean.createSqlUpdate(sql)
      .setGetGeneratedKeys(true)
      .setParameter(1, email)
      .setParameter(2, true)
      .setParameter(3, true)
      .executeGetKey();

    EPersonOnline found = Ebean.find(EPersonOnline.class, key);
    assertThat(found).isNotNull();
    assertThat(found.getEmail()).isEqualTo("foo@one.com");
    assertThat(found.isOnline()).isTrue();


    String sqlNamed = "insert into e_person_online (email, online, when_updated) values (:email, :online, now()) on conflict (email) do update set when_updated=now(), online = :online";
    SqlUpdate sqlUpdate2 = Ebean.createSqlUpdate(sqlNamed)
      .setGetGeneratedKeys(true)
      .setParameter("email", email)
      .setParameter("online", false);

    Object key2 = sqlUpdate2.executeGetKey();

    EPersonOnline found2 = Ebean.find(EPersonOnline.class, key2);
    assertThat(found2).isNotNull();
    assertThat(found2.getId()).isEqualTo(key);
    assertThat(found2.getEmail()).isEqualTo("foo@one.com");
    assertThat(found2.isOnline()).isFalse();
    assertThat(found2.getWhenUpdated()).isGreaterThan(found.getWhenUpdated());

  }

  @ForPlatform(Platform.MYSQL)
  @Test
  public void mySqlUpsert() {

    String email = "bar@one.com";

    String sql = "insert into e_person_online (email, online, when_updated) values (?, ?, current_time) on duplicate key update when_updated=current_time, online = ?";
    SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql)
      .setGetGeneratedKeys(true)
      .setParameter(1, email)
      .setParameter(2, true)
      .setParameter(3, true);

    Object key = sqlUpdate.executeGetKey();
    assertThat(key).isNotNull();

    EPersonOnline found = Ebean.find(EPersonOnline.class, key);
    assertThat(found).isNotNull();
    assertThat(found.getEmail()).isEqualTo("bar@one.com");
    assertThat(found.isOnline()).isTrue();


    String sqlNamed = "insert into e_person_online (email, online, when_updated) values (:email, :online, current_time) on duplicate key update when_updated=current_time, online = :online";
    SqlUpdate sqlUpdate2 = Ebean.createSqlUpdate(sqlNamed)
      .setGetGeneratedKeys(true)
      .setParameter("email", email)
      .setParameter("online", false);

    sqlUpdate2.execute();
    Object key2 = sqlUpdate2.getGeneratedKey();

    EPersonOnline found2 = Ebean.find(EPersonOnline.class, key2);
    assertThat(found2).isNotNull();
    assertThat(found2.getId()).isEqualTo(key);
    assertThat(found2.getEmail()).isEqualTo("bar@one.com");
    assertThat(found2.isOnline()).isFalse();

  }
}
