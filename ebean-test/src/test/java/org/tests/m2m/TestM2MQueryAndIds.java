package org.tests.m2m;


import io.ebean.DB;
import io.ebean.Query;
import io.ebean.annotation.Platform;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.ForPlatform;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.model.m2m.Permission;
import org.tests.model.m2m.Role;
import org.tests.model.m2m.Tenant;

import java.util.List;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

public class TestM2MQueryAndIds extends BaseTestCase {

  @BeforeAll
  public static void beforeAll() throws Exception {
    Tenant tenant = new Tenant("TestTenant");
    DB.save(tenant);

    Permission perm = new Permission("TestPerm");
    DB.save(perm);

    Role role = new Role("TestRole");
    role.getPermissions().add(perm);
    role.setTenant(tenant);
    DB.save(role);
  }

  @AfterAll
  public static void afterAll() {
    DB.find(Role.class).delete();
    DB.find(Permission.class).delete();
    DB.find(Tenant.class).delete();
  }

  @ForPlatform(Platform.H2)
  @Test
  public void testM2MWithId() {
    Query<Permission> sq1 = DB.find(Permission.class)
      .alias("sq1")
      .where()
      .eq("name", "TestPerm")
      .raw("(sq1.id = permissions.id)")
      .query();

    Query<Role> query = DB.find(Role.class).select("id").where().exists(sq1).query();
    List<Role> models = query.findList();
    assertThat(models).hasSize(1);
    assertThat(query.getGeneratedSql()).isEqualTo("select distinct t0.id from mt_role t0 left join mt_role_permission t1z_ on t1z_.mt_role_id = t0.id left join mt_permission t1 on t1.id = t1z_.mt_permission_id where exists (select 1 from mt_permission sq1 where sq1.name = ? and (sq1.id = t1.id))");
  }

  @ForPlatform(Platform.H2)
  @Test
  public void testM2MWithoutId() {
    Query<Permission> sq1 = DB.find(Permission.class)
      .alias("sq1")
      .where()
      .eq("name", "TestPerm")
      .raw("(sq1.id = permissions)")
      .query();

    Query<Role> query = DB.find(Role.class).select("id").where().exists(sq1).query();
    List<Role> models = query.findList();
    assertThat(models).hasSize(1);
    // query from testO2MWith...Id():                       select          t0.id from mt_role t0                                                              where exists (select 1 from mt_permission sq1 where sq1.name = ? and (sq1.id = t0.tenant_id))
    assertThat(query.getGeneratedSql()).isEqualTo("select distinct t0.id from mt_role t0 left join mt_role_permission t1z_ on t1z_.mt_role_id = t0.id where exists (select 1 from mt_permission sq1 where sq1.name = ? and (sq1.id = t1z_.mt_permission_id))");
  }

  @ForPlatform(Platform.H2)
  @Test
  public void testO2MWithId() {
    Query<Tenant> sq1 = DB.find(Tenant.class)
      .alias("sq1")
      .where()
      .eq("name", "TestTenant")
      .raw("(sq1.id = tenant.id)")
      .query();

    Query<Role> query = DB.find(Role.class).select("id").where().exists(sq1).query();
    List<Role> models = query.findList();
    assertThat(models).hasSize(1);
    assertThat(query.getGeneratedSql()).isEqualTo("select t0.id from mt_role t0 where exists (select 1 from mt_tenant sq1 where sq1.name = ? and (sq1.id = t0.tenant_id))");
  }

  @ForPlatform(Platform.H2)
  @Test
  public void testO2MWithoutId() {
    Query<Tenant> sq1 = DB.find(Tenant.class)
      .alias("sq1")
      .where()
      .eq("name", "TestTenant")
      .raw("(sq1.id = tenant)")
      .query();

    Query<Role> query = DB.find(Role.class).select("id").where().exists(sq1).query();
    List<Role> models = query.findList();
    assertThat(models).hasSize(1);
    assertThat(query.getGeneratedSql()).isEqualTo("select t0.id from mt_role t0 where exists (select 1 from mt_tenant sq1 where sq1.name = ? and (sq1.id = t0.tenant_id))");
  }
}
