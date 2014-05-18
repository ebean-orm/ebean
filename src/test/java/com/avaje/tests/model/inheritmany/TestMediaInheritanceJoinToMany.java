package com.avaje.tests.model.inheritmany;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;

public class TestMediaInheritanceJoinToMany extends BaseTestCase {

  @Test
  public void test() {
    
    
    String name = "nopic";
    
    MProfile profileWithNoPic = new MProfile();
    profileWithNoPic.setName(name);
    
    Ebean.save(profileWithNoPic);
    
    Query<MProfile> query = Ebean.find(MProfile.class).fetch("picture").where().eq("name", name).query();
    
    // assert we get the profile with a null picture
    MProfile profile = query.findUnique();
    Assert.assertNotNull(profile);
    
    // select t0.id c0, t0.name c1, t1.type c2, t1.id c3, t1.url c4, t1.note c5 
    // from profile t0 
    // left outer join media t1 on t1.id = t0.picture_id and t1.type = 'Picture'  
    // where t0.name = ? ; --bind(nopic)
    
    // specifically t1.type = 'Picture' ... on on the join and not in the where
   
    String generatedSql = query.getGeneratedSql();
    Assert.assertTrue(generatedSql.contains("from mprofile t0 left outer join mmedia t1 on t1.id = t0.picture_id and t1.type = 'Picture' "));
    Assert.assertTrue(generatedSql.contains("where t0.name = ? "));
    
  }
  
}
