package com.avaje.tests.basic;

import java.util.List;

import junit.framework.TestCase;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.common.BeanList;
import com.avaje.tests.model.basic.MRole;
import com.avaje.tests.model.basic.MUser;

public class TestM2MCascadeOne extends TestCase {

    public void test() {
        
        MUser u = new MUser();
        u.setUserName("testM2M");
        
        List<MRole> roles = u.getRoles();
        if (roles != null){
            if (roles instanceof BeanList<?>){
                System.out.println("enhancement checkNullManyFields=true successful");
            }
        }
        
        Ebean.save(u);
        
        MRole r0 = new MRole();
        r0.setRoleName("rol_0");
        Ebean.save(r0);
        
        MRole r1 = new MRole();
        r1.setRoleName("rol_1");
        
        MUser u1 = Ebean.find(MUser.class, u.getUserid());
        
        u1.addRole(r0);
        u1.addRole(r1);
        
        Ebean.save(u1);
        
    }
}
