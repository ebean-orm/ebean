package org.tests.model.bridge;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TestIdClassScalar extends BaseTestCase {

  private final BUser user = new BUser("Fiona");
  private final BSite site = new BSite("avaje.io");

  @Test
  void testBEmbId_equalsHashcode() {

    BEmbId a = new BEmbId(UUID.randomUUID(), UUID.randomUUID());
    BEmbId b = new BEmbId(a.getSiteId(), a.getUserId());

    BEmbId c = new BEmbId(UUID.randomUUID(), UUID.randomUUID());

    BEmbId d = new BEmbId(a.getSiteId(), UUID.randomUUID());

    assertThat(a).isEqualTo(b);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());

    assertThat(a).isNotEqualTo(c);
    assertThat(a.hashCode()).isNotEqualTo(c.hashCode());

    assertThat(a).isNotEqualTo(d);
    assertThat(a.hashCode()).isNotEqualTo(d.hashCode());

    assertThat(a.hashCode()).isEqualTo(a.otherHash());
  }

  @Test
  void fetchMany() {
    UUID siteId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    BSiteUserD access = new BSiteUserD(BAccessLevel.ONE, siteId, userId);
    DB.save(access);

    final List<BSiteUserD> found = DB.find(BSiteUserD.class)
      .fetch("children")
      .where().eq("siteId", siteId)
      .findList();

    assertThat(found).hasSize(1);

    DB.delete(BSiteUserD.class, new BEmbId(siteId, userId));
  }

  @Test
  void insertBatch() {
    UUID siteId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    try (final Transaction transaction = DB.beginTransaction()) {
      transaction.setBatchMode(true);


      BSiteUserD access = new BSiteUserD(BAccessLevel.ONE, siteId, userId);
      DB.save(access);

      final UUID siteId1 = access.getSiteId(); // ArrayIndexOutOfBoundsException here
      assertThat(siteId1).isNotNull();
      assertThat(access.getUserId()).isEqualTo(userId);

      transaction.commit();
    }

    DB.delete(BSiteUserD.class, new BEmbId(siteId, userId));
  }

  @Test
  void test() {
    DB.save(user);
    DB.save(site);
    insertUpdateBridgeD(user, site);
    insertUpdateBridgeE(user, site);
  }

  /**
   * Test where matching by db column naming convention.
   */
  private void insertUpdateBridgeD(BUser user, BSite site) {
    LoggedSql.start();

    BSiteUserD access = new BSiteUserD(BAccessLevel.ONE, site.id, user.id);
    DB.save(access);

    access.setAccessLevel(BAccessLevel.TWO);
    DB.save(access);

    List<BSiteUserD> list = DB.find(BSiteUserD.class).findList();
    assertThat(list).isNotEmpty();

    for (BSiteUserD bridge : list) {
      assertThat(bridge.getSiteId()).isEqualTo(site.id);
      assertThat(bridge.getUserId()).isEqualTo(user.id);
    }

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(3);
    assertSql(sql.get(0)).contains("insert into bsite_user_d (site_id, user_id, access_level, version) values (?,?,?,?)");
    assertSql(sql.get(1)).contains("update bsite_user_d set access_level=?, version=? where site_id=? and user_id=? and version=?");
    assertSql(sql.get(2)).contains("select t0.site_id, t0.user_id, t0.site_id, t0.user_id, t0.access_level, t0.version from bsite_user_d t0");


    BEmbId id = new BEmbId(site.id, user.id);
    BSiteUserD one = DB.find(BSiteUserD.class, id);

    assertThat(one).isNotNull();
    assertThat(one.getSiteId()).isEqualTo(site.id);
    assertThat(one.getUserId()).isEqualTo(user.id);

    one.setAccessLevel(BAccessLevel.THREE);
    DB.save(one);

    sql = LoggedSql.stop();

    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select t0.site_id, t0.user_id, t0.site_id, t0.user_id, t0.access_level, t0.version from bsite_user_d t0 where t0.site_id=? and t0.user_id=?");
    assertSql(sql.get(1)).contains("update bsite_user_d set access_level=?, version=? where site_id=? and user_id=? and version=?");
  }

  /**
   * Test where matching by db column naming convention.
   */
  private void insertUpdateBridgeE(BUser user, BSite site) {
    LoggedSql.start();

    BSiteUserE access = new BSiteUserE(BAccessLevel.ONE, site, user);
    DB.save(access);

    access.setAccessLevel(BAccessLevel.TWO);
    DB.save(access);

    List<BSiteUserE> list = DB.find(BSiteUserE.class).findList();
    assertThat(list).isNotEmpty();

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(3);
    assertSql(sql.get(0)).contains("insert into bsite_user_e (site_id, user_id, access_level)");
    assertSql(sql.get(1)).contains("update bsite_user_e set access_level=? where site_id=? and user_id=?");
    assertSql(sql.get(2)).contains("select t0.site_id, t0.user_id, t0.access_level, t0.site_id, t0.user_id from bsite_user_e t0");


    for (BSiteUserE bridge : list) {
      assertThat(bridge.getSite().id).isEqualTo(site.id);
      assertThat(bridge.getUser().id).isEqualTo(user.id);
    }

    BEmbId id = new BEmbId(site.id, user.id);

    BSiteUserE one = DB.find(BSiteUserE.class, id);

    assertThat(one).isNotNull();
    assertThat(one.getSite().id).isEqualTo(site.id);
    assertThat(one.getUser().id).isEqualTo(user.id);

    one.setAccessLevel(BAccessLevel.THREE);
    DB.save(one);

    sql = LoggedSql.stop();

    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select t0.site_id, t0.user_id, t0.access_level, t0.site_id, t0.user_id from bsite_user_e t0 where t0.site_id=? and t0.user_id=?");
    assertSql(sql.get(1)).contains("update bsite_user_e set access_level=? where site_id=? and user_id=?");

  }

  @Test
  void idClass_existsQuery() {
    BUser user = new BUser("JunkUser2");
    BSite site = new BSite("JunkSite2");
    BSiteUserE access = new BSiteUserE(BAccessLevel.ONE, site, user);

    DB.save(user);
    DB.save(site);
    DB.save(access);

    boolean exists = DB.find(BSiteUserE.class)
      .where().eq("user.id", user.id)
      .exists();

    assertThat(exists).isTrue();
    assertThat(DB.find(BSiteUserE.class).where().eq("user.id", user.id).findCount()).isEqualTo(1);

    DB.delete(access);
    DB.deleteAll(Arrays.asList(user, site));
  }

  @Test
  void idClass_existsQuery_scalarImportedProperties() {
    BUser user = new BUser("JunkUser3");
    BSite site = new BSite("JunkSite3");
    DB.save(user);
    DB.save(site);

    BSiteUserD access = new BSiteUserD(BAccessLevel.ONE, site.id, user.id);
    DB.save(access);

    boolean exists = DB.find(BSiteUserD.class)
      .where().eq("userId", user.id)
      .exists();

    assertThat(exists).isTrue();
    assertThat(DB.find(BSiteUserD.class).where().eq("userId", user.id).findCount()).isEqualTo(1);

    DB.delete(access);
    DB.deleteAll(Arrays.asList(user, site));
  }

}
