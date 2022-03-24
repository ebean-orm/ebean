package org.tests.m2m;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.meta.MetaTimedMetric;
import org.junit.jupiter.api.Test;
import org.tests.model.m2m.Permission;
import org.tests.model.m2m.Role;
import org.tests.model.m2m.Tenant;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestM2mDeleteObject extends BaseTestCase {

  @Test
  public void test() {

    resetAllMetrics();

    DB.createUpdate(Permission.class, "delete from Permission").setLabel("deleteAllPermissions").execute();
    DB.createUpdate(Tenant.class, "delete from Tenant").execute();
    DB.createUpdate(Role.class, "delete from Role").execute();

    List<MetaTimedMetric> sqlMetrics = sqlMetrics();
    assertThat(sqlMetrics).hasSize(1);
    assertThat(sqlMetrics.get(0).name()).isEqualTo("orm.update.deleteAllPermissions");

    Tenant t = new Tenant("tenant");

    DB.save(t);

    Permission p1 = new Permission("p1");
    Permission p2 = new Permission("p2");

    DB.save(p1);
    DB.save(p2);

    Role role1 = new Role("role");
    role1.setTenant(t);

    Set<Permission> permissions = new HashSet<>();
    List<Permission> permsList = DB.find(Permission.class).findList();
    permissions.addAll(permsList);

    role1.setPermissions(permissions);

    DB.save(role1);

    List<Tenant> tenantList = DB.find(Tenant.class).fetch("roles").findList();
    List<Role> roleList = DB.find(Role.class).fetch("permissions").findList();
    List<Permission> permissionList = DB.find(Permission.class).fetch("roles").findList();

    assertEquals(1, tenantList.size());
    assertEquals(2, permissionList.size());
    assertEquals(1, roleList.size());

    DB.delete(role1);

    List<Tenant> tenantList2 = DB.find(Tenant.class).fetch("roles").findList();
    List<Role> roleList2 = DB.find(Role.class).fetch("permissions").findList();
    List<Permission> permissionList2 = DB.find(Permission.class).fetch("roles").findList();

    assertEquals(0, roleList2.size());
    assertEquals(1, tenantList2.size());
    assertEquals(2, permissionList2.size());
  }


  @Test
  public void test_deleteCascading() {

    Permission p1 = new Permission("p1");
    Permission p2 = new Permission("p2");
    DB.save(p1);
    DB.save(p2);

    Role role1 = new Role("role");

    Set<Permission> permissions = new HashSet<>();
    permissions.add(p2);
    role1.setPermissions(permissions);
    DB.save(role1);

    // only deletes as not associated
    DB.delete(p1);
    // delete cascades
    DB.delete(role1);
  }
}
