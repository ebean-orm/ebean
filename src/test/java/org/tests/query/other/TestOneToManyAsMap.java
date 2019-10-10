package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.map.MpRole;
import org.tests.model.map.MpUser;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToManyAsMap extends BaseTestCase {

  @Test
  public void test() {

    MpUser u = new MpUser();
    Ebean.save(u);

    MpUser u2 = Ebean.find(MpUser.class, u.getId());
    Assert.assertNotNull(u2);

    u2.setName("Charlie Brown");
    MpRole ourl = new MpRole();
    ourl.setOrganizationId(47L);
    u2.getRoles().put("one", ourl);
    Ebean.save(u2);

    MpUser u3 = Ebean.find(MpUser.class, u.getId());
    Assert.assertEquals("Charlie Brown", u3.getName());

    Map<String, MpRole> listMap = u3.getRoles();
    Assert.assertEquals(1, listMap.size());

    MpRole mpRole = listMap.get("one");
    Assert.assertNotNull(mpRole);
    Assert.assertEquals(Long.valueOf(47L), mpRole.getOrganizationId());
    Assert.assertEquals("one", mpRole.getCode());

  }

  @Test
  public void json() {

    Map<String,MpRole> roles = new LinkedHashMap<>();
    roles.put("r1", newRole(1L, "r1"));
    roles.put("r2", newRole(2L, "r2"));

    MpUser u = new MpUser();
    u.setName("myName");
    u.setRoles(roles);

    final String asJson = DB.json().toJson(u);

    assertThat(asJson).isEqualTo("{\"name\":\"myName\",\"roles\":[{\"id\":1,\"code\":\"r1\"},{\"id\":2,\"code\":\"r2\"}]}");

    final MpUser bean = DB.json().toBean(MpUser.class, asJson);
    assertThat(bean.getName()).isEqualTo("myName");
    final Map<String, MpRole> beanRoles = bean.getRoles();
    assertThat(beanRoles.get("r1").getId()).isEqualTo(1L);
    assertThat(beanRoles.get("r1").getCode()).isEqualTo("r1");
    assertThat(beanRoles.get("r2").getId()).isEqualTo(2L);
    assertThat(beanRoles.get("r2").getCode()).isEqualTo("r2");
  }

  private MpRole newRole(Long id, String code) {
    MpRole role = new MpRole();
    role.setId(id);
    role.setCode(code);
    return role;
  }

}
