package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.tests.model.basic.MRole;
import org.tests.model.basic.MUser;

public class TestM2MCascadeOne extends BaseTestCase {
  
  @Test
  public void test() {

    MUser u = new MUser();
    u.setUserName("testM2M");

    u.getRoles();
    DB.save(u);

    MRole r0 = new MRole();
    r0.setRoleName("rol_0");
    DB.save(r0);

    MRole r1 = new MRole();
    r1.setRoleName("rol_1");

    MUser u1 = DB.find(MUser.class, u.getUserid());

    u1.addRole(r0);
    u1.addRole(r1);

    TPersistVisitor tv = new TPersistVisitor();
    DB.visitSave(u1, tv);
    assertThat(tv.toString()).isEqualTo("<root>\n"
        + "  <bean type='MUser' newOrDirty='false'>\n"
        + "    <property name='roles'>\n"
        + "      <collection size='2'>\n"
        + "        <bean type='MRole' newOrDirty='false'/>\n"
        + "        <bean type='MRole' newOrDirty='true'/>\n"
        + "      </collection>\n"
        + "    </property>\n"
        + "  </bean>\n"
        + "</root>\n");
    
    DB.save(u1);
    
    u1 = DB.find(MUser.class, u.getUserid());
    
    tv = new TPersistVisitor();
    DB.visitSave(u1, tv);
    // collection is unloaded
    assertThat(tv.toString()).isEqualTo("<root>\n"
        + "  <bean type='MUser' newOrDirty='false'/>\n"
        + "</root>\n");
    
    
  }

  @Test
  public void testRawPredicate_with_ManyToManyPath() {

    Query<MUser> query = DB.find(MUser.class)
      .select("userid")
      .where().raw("roles.roleid in (?)", 24)
      .query();

    query.findList();

  }
}
