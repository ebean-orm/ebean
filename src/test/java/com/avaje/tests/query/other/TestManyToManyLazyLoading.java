package com.avaje.tests.query.other;

import java.util.List;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.MRole;
import com.avaje.tests.model.basic.MUser;

public class TestManyToManyLazyLoading  extends BaseTestCase {

  @Test
  public void test() {
    
    createData();
    
    List<MUser> users = Ebean.find(MUser.class).findList();
    
    for (MUser user : users) {
      List<MRole> roles = user.getRoles();
      System.out.println(""+roles.size());
    }
    
  }
  
  private void  createData() {
    MRole r0 = new MRole("r0");
    MRole r1 = new MRole("r1");

    Ebean.save(r0);
    Ebean.save(r1);

    MUser u0 = new MUser("usr0");
    u0.addRole(r0);
    u0.addRole(r1);

    Ebean.save(u0);
  }
  
}
