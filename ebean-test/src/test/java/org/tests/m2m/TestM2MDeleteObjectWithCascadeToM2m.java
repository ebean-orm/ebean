package org.tests.m2m;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.m2m.Permission;
import org.tests.model.m2m.Role;
import org.tests.model.m2m.Tenant;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestM2MDeleteObjectWithCascadeToM2m extends BaseTestCase {

  @Test
  public void test() {

    DB.createUpdate(Permission.class, "delete from Permission").execute();
    DB.createUpdate(Tenant.class, "delete from Tenant").execute();
    DB.createUpdate(Role.class, "delete from Role").execute();

    Tenant tenant1 = new Tenant("Tenant");
    DB.save(tenant1);

    Permission p1 = new Permission("p1");
    Permission p2 = new Permission("p2");
    DB.save(p1);
    DB.save(p2);

    Role role1 = new Role("RoleOne");
    role1.setTenant(tenant1);

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

    DB.delete(tenant1);

    List<Tenant> tenantList2 = DB.find(Tenant.class).fetch("roles").findList();
    List<Role> roleList2 = DB.find(Role.class).fetch("permissions").findList();
    List<Permission> permissionList2 = DB.find(Permission.class).fetch("roles").findList();

    assertEquals(0, tenantList2.size());
    assertEquals(0, roleList2.size());
    assertEquals(2, permissionList2.size());

  }
}
