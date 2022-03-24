package org.tests.model.bridge;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestExplicitM2MBridgeTable extends BaseTestCase {

  @Test
  public void test() {

    BUser user = new BUser("Rob");
    BSite site = new BSite("avaje.org");

    DB.save(user);
    DB.save(site);

    insertUpdateBridgeA(user, site);
    insertUpdateBridgeB(user, site);
    insertUpdateBridgeC(user, site);
  }

  /**
   * Test where matching by db column naming convention.
   */
  private void insertUpdateBridgeA(BUser user, BSite site) {

    BSiteUserA access = new BSiteUserA(BAccessLevel.ONE, site, user);
    DB.save(access);

    access.setAccessLevel(BAccessLevel.TWO);
    DB.save(access);

    List<BSiteUserA> list = DB.find(BSiteUserA.class).findList();
    assertThat(list).isNotEmpty();

    for (BSiteUserA bridge : list) {
      assertThat(bridge.getId().siteId).isEqualTo(bridge.getSite().id);
      assertThat(bridge.getId().userId).isEqualTo(bridge.getUser().id);
    }

    BSiteUserA found = DB.find(BSiteUserA.class, new BSiteUserA.Id(site.id, user.id));
    found.setAccessLevel(BAccessLevel.THREE);

    DB.save(found);
  }

  /**
   * Test where matching by property name.
   */
  private void insertUpdateBridgeB(BUser user, BSite site) {

    BSiteUserB access = new BSiteUserB(BAccessLevel.ONE, site, user);
    DB.save(access);

    access.setAccessLevel(BAccessLevel.TWO);
    DB.save(access);

    List<BSiteUserB> list = DB.find(BSiteUserB.class).findList();
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
    DB.save(access);

    access.setAccessLevel(BAccessLevel.TWO);
    DB.save(access);

    List<BSiteUserC> list = DB.find(BSiteUserC.class).findList();
    assertThat(list).isNotEmpty();

    for (BSiteUserC bridge : list) {
      assertThat(bridge.getId().siteUid).isEqualTo(bridge.getSite().id);
      assertThat(bridge.getId().userUid).isEqualTo(bridge.getUser().id);
    }
  }
}
