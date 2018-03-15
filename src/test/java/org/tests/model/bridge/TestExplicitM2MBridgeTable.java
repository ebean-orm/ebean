package org.tests.model.bridge;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestExplicitM2MBridgeTable extends BaseTestCase {

  @Test
  public void test() {

    BUser user = new BUser("Rob");
    BSite site = new BSite("avaje.org");

    Ebean.save(user);
    Ebean.save(site);

    insertUpdateBridgeA(user, site);
    insertUpdateBridgeB(user, site);
    insertUpdateBridgeC(user, site);
  }

  /**
   * Test where matching by db column naming convention.
   */
  private void insertUpdateBridgeA(BUser user, BSite site) {

    BSiteUserA access = new BSiteUserA(BAccessLevel.ONE, site, user);
    Ebean.save(access);

    access.setAccessLevel(BAccessLevel.TWO);
    Ebean.save(access);

    List<BSiteUserA> list = Ebean.find(BSiteUserA.class).findList();
    assertThat(list).isNotEmpty();

    for (BSiteUserA bridge : list) {
      assertThat(bridge.getId().siteId).isEqualTo(bridge.getSite().id);
      assertThat(bridge.getId().userId).isEqualTo(bridge.getUser().id);
    }

    BSiteUserA found = Ebean.find(BSiteUserA.class, new BSiteUserA.Id(site.id, user.id));
    found.setAccessLevel(BAccessLevel.THREE);

    Ebean.save(found);
  }

  /**
   * Test where matching by property name.
   */
  private void insertUpdateBridgeB(BUser user, BSite site) {

    BSiteUserB access = new BSiteUserB(BAccessLevel.ONE, site, user);
    Ebean.save(access);

    access.setAccessLevel(BAccessLevel.TWO);
    Ebean.save(access);

    List<BSiteUserB> list = Ebean.find(BSiteUserB.class).findList();
    assertThat(list).isNotEmpty();

    for (BSiteUserB bridge : list) {
      assertThat(bridge.getId().site).isEqualTo(bridge.getSite().id);
      assertThat(bridge.getId().user).isEqualTo(bridge.getUser().id);
    }
  }

  /**
   * Test where matching by explicit @JoinColumn.
   */
  private void insertUpdateBridgeC(BUser user, BSite site) {

    BSiteUserC access = new BSiteUserC(BAccessLevel.ONE, site, user);
    Ebean.save(access);

    access.setAccessLevel(BAccessLevel.TWO);
    Ebean.save(access);

    List<BSiteUserC> list = Ebean.find(BSiteUserC.class).findList();
    assertThat(list).isNotEmpty();

    for (BSiteUserC bridge : list) {
      assertThat(bridge.getId().siteUid).isEqualTo(bridge.getSite().id);
      assertThat(bridge.getId().userUid).isEqualTo(bridge.getUser().id);
    }
  }
}
