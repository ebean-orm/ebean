package com.avaje.tests.query.orderby;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.MRole;
import com.avaje.tests.model.basic.MUser;
import com.avaje.tests.model.basic.MUserType;


public class TestOrderByWithDistinct extends BaseTestCase {
	
	@Test
	public void test() {
		/*
		 * Original conversation:
		 * https://groups.google.com/forum/?fromgroups=#!topic/ebean/uuvi1btdCDQ%5B1-25-false%5D
		 * 
		 * This test exposes what may be a general problem with columns required by the order by phrase being omitted from the select.
		 * I'm not sure this exposes all causes of the problem.
		 */
		
		MUserType ut = new MUserType("md");
		Ebean.save(ut);
		MUser user1 = new MUser("one");
		user1.setUserType(ut);
		Ebean.save(user1);
		MUser user2 = new MUser("two");
		user2.setUserType(ut);
		Ebean.save(user2);
		
		MRole roleA = new MRole("A");
		Ebean.save(roleA);
		MRole roleB = new MRole("B");
		Ebean.save(roleB);
		
		user1.addRole(roleA);
		Ebean.save(user1);
		user2.addRole(roleB);
		Ebean.save(user2);
		
		Query<MUser> query = Ebean.find(MUser.class)
		    .fetch("userType","name")
				.where()
				.eq("roles.roleName", "A")
				.orderBy("userType.name, userName");
		List<MUser> list = query.findList();
		
		// select distinct t0.userid c0, t0.user_name c1, t1.id c2, t1.name c3 
		// from muser t0 
		// left outer join muser_type t1 on t1.id = t0.user_type_id  
		// join mrole_muser u1z_ on u1z_.muser_userid = t0.userid 
		// join mrole u1 on u1.roleid = u1z_.mrole_roleid 
		// where u1.role_name = ? 
		// order by t1.name, t0.user_name; --bind(A)
		
		Assert.assertEquals(1, list.size());
		Assert.assertEquals(user1, list.get(0));
    String generatedSql = query.getGeneratedSql();
    Assert.assertTrue(generatedSql.contains("select distinct t0.userid")); // using distinct
    Assert.assertTrue(generatedSql.contains("order by t1.name,")); // name in order by
    Assert.assertTrue(generatedSql.contains("t1.name c"));// name in select
		
		
		
		// repeat with slight variation, not sure this really produces a different execution path
		// this problem also manifests when autofetch eliminates properties from the select that aren't used in the objects
		// still need them to be present for purpose of order by
		// so here I'm "simulating" a scenario where autofetch has dropped userType.name
		query = Ebean.find(MUser.class)
				.setAutofetch(false)
				.select("userName")
				.fetch("userType","name")
				.where()
				.eq("roles.roleName", "A")
				.orderBy("userType.name");
		list = query.findList();
				
		Assert.assertEquals(1, list.size());
		Assert.assertEquals(user1, list.get(0));
		
		// select distinct t0.userid c0, t0.user_name c1, t1.id c2, t1.name c3 
		// from muser t0 
		// left outer join muser_type t1 on t1.id = t0.user_type_id 
		// join mrole_muser u1z_ on u1z_.muser_userid = t0.userid 
		// join mrole u1 on u1.roleid = u1z_.mrole_roleid  
		// where u1.role_name = ? 
		// order by t1.name; --bind(A)
		
		generatedSql = query.getGeneratedSql();
    Assert.assertTrue(generatedSql.contains("select distinct t0.userid")); // using distinct
    Assert.assertTrue(generatedSql.contains("order by t1.name")); // name in order by
    Assert.assertTrue(generatedSql.contains("t1.name c"));// name in select
		
	}

}
