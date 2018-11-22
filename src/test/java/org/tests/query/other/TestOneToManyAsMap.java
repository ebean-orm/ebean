package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.map.MpRole;
import org.tests.model.map.MpUser;

import java.util.Map;

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

}
