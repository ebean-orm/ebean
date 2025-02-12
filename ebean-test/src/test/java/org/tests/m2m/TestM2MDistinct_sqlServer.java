package org.tests.m2m;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.xtest.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.m2m.Permission;
import org.tests.model.m2m.Role;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestM2MDistinct_sqlServer extends BaseTestCase {

  @ForPlatform(Platform.SQLSERVER)
  @Test
  public void testName() {

    Permission perm = new Permission("TestPerm");
    DB.save(perm);
    LoggedSql.start();

    List<Role> roles = DB.find(Role.class).where().eq("permissions", perm)
      .orderBy().asc("name", "Latin1_General_CI_AS")
      .findList();

    List<String> sqls = LoggedSql.stop();
    assertThat(sqls.get(0)).startsWith("select distinct t0.id, t0.name, t0.version, t0.tenant_id, t0.name collate Latin1_General_CI_AS "
      + "from mt_role t0 join mt_role_permission u1z_ on u1z_.mt_role_id = t0.id "
      + "join mt_permission u1 on u1.id = u1z_.mt_permission_id where u1.id = ? "
      + "order by t0.name collate Latin1_General_CI_AS;");
  }
}
