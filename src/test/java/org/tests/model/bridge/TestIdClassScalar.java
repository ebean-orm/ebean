package org.tests.model.bridge;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TestIdClassScalar extends BaseTestCase {

  private BUser user = new BUser("Fiona");
  private BSite site = new BSite("avaje.io");

  @Test
  public void testBEmbId_equalsHashcode() {

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
  public void test() {

    Ebean.save(user);
    Ebean.save(site);

    insertUpdateBridgeD(user, site);
    insertUpdateBridgeE(user, site);
  }

  /**
   * Test where matching by db column naming convention.
   */
  private void insertUpdateBridgeD(BUser user, BSite site) {

    LoggedSqlCollector.start();

    BSiteUserD access = new BSiteUserD(BAccessLevel.ONE, site.id, user.id);
    Ebean.save(access);

    access.setAccessLevel(BAccessLevel.TWO);
    Ebean.save(access);

    List<BSiteUserD> list = Ebean.find(BSiteUserD.class).findList();
    assertThat(list).isNotEmpty();

    for (BSiteUserD bridge : list) {
      assertThat(bridge.getSiteId()).isEqualTo(site.id);
      assertThat(bridge.getUserId()).isEqualTo(user.id);
    }

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(3);
    assertThat(sql.get(0)).contains("insert into bsite_user_d (site_id, user_id, access_level, version) values (?,?,?,?)");
    assertThat(sql.get(1)).contains("update bsite_user_d set access_level=?, version=? where site_id=? and user_id=? and version=?");
    assertThat(trimSql(sql.get(2))).contains("select t0.site_id, t0.user_id, t0.site_id, t0.user_id, t0.access_level, t0.version from bsite_user_d t0");


    BEmbId id = new BEmbId(site.id, user.id);
    BSiteUserD one = Ebean.find(BSiteUserD.class, id);

    assertThat(one).isNotNull();
    assertThat(one.getSiteId()).isEqualTo(site.id);
    assertThat(one.getUserId()).isEqualTo(user.id);

    one.setAccessLevel(BAccessLevel.THREE);
    Ebean.save(one);

    sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(2);
    assertThat(trimSql(sql.get(0))).contains("select t0.site_id, t0.user_id, t0.site_id, t0.user_id, t0.access_level, t0.version from bsite_user_d t0 where t0.site_id = ?  and t0.user_id = ?");
    assertThat(sql.get(1)).contains("update bsite_user_d set access_level=?, version=? where site_id=? and user_id=? and version=?");
  }

  /**
   * Test where matching by db column naming convention.
   */
  private void insertUpdateBridgeE(BUser user, BSite site) {

    LoggedSqlCollector.start();

    BSiteUserE access = new BSiteUserE(BAccessLevel.ONE, site, user);
    Ebean.save(access);

    access.setAccessLevel(BAccessLevel.TWO);
    Ebean.save(access);

    List<BSiteUserE> list = Ebean.find(BSiteUserE.class).findList();
    assertThat(list).isNotEmpty();

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(3);
    assertThat(sql.get(0)).contains("insert into bsite_user_e (site_id, user_id, access_level)");
    assertThat(sql.get(1)).contains("update bsite_user_e set access_level=? where site_id=? and user_id=?");
    assertThat(trimSql(sql.get(2))).contains("select t0.site_id, t0.user_id, t0.access_level, t0.site_id, t0.user_id from bsite_user_e t0");


    for (BSiteUserE bridge : list) {
      assertThat(bridge.getSite().id).isEqualTo(site.id);
      assertThat(bridge.getUser().id).isEqualTo(user.id);
    }

    BEmbId id = new BEmbId(site.id, user.id);

    BSiteUserE one = Ebean.find(BSiteUserE.class, id);

    assertThat(one).isNotNull();
    assertThat(one.getSite().id).isEqualTo(site.id);
    assertThat(one.getUser().id).isEqualTo(user.id);

    one.setAccessLevel(BAccessLevel.THREE);
    Ebean.save(one);

    sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(2);
    assertThat(trimSql(sql.get(0))).contains("select t0.site_id, t0.user_id, t0.access_level, t0.site_id, t0.user_id from bsite_user_e t0 where t0.site_id = ?  and t0.user_id = ?");
    assertThat(sql.get(1)).contains("update bsite_user_e set access_level=? where site_id=? and user_id=?");

  }
}
