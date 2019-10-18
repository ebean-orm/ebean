package org.tests.m2m;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.ebeantest.LoggedSqlCollector;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tests.model.basic.MnocRole;
import org.tests.model.basic.MnocUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestM2MDeleteNoCascade extends BaseTestCase {

  private static MnocRole r0 = new MnocRole("r0");
  private static MnocRole r1 = new MnocRole("r1");
  private static MnocRole r2 = new MnocRole("r2");

  @BeforeClass
  public static void setup() {
    Ebean.save(r0);
    Ebean.save(r1);
    Ebean.save(r2);
  }

  @IgnorePlatform(Platform.NUODB)
  @Test
  public void test() {

    MnocUser u0 = new MnocUser("usr0");
    u0.addValidRole(r0);
    u0.addValidRole(r1);

    Ebean.save(u0);

    MnocUser loadedUser = Ebean.find(MnocUser.class, u0.getUserId());
    List<MnocRole> validRoles = loadedUser.getValidRoles();
    assertThat(validRoles).hasSize(2);

    LoggedSqlCollector.start();
    Ebean.delete(u0);

    final List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from mnoc_user_mnoc_role where mnoc_user_user_id = ?");
    assertThat(sql.get(1)).contains("delete from mnoc_user where user_id=? and version=?");

    final MnocUser found = Ebean.find(MnocUser.class, u0.getUserId());
    assertThat(found).isNull();
  }
}
