package org.tests.model.inheritmany;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestMediaInheritanceJoinToMany extends BaseTestCase {

  @Test
  public void test() {

    String name = "nopic" + new Random().nextInt();

    MProfile profileWithNoPic = new MProfile();
    profileWithNoPic.setName(name);

    DB.save(profileWithNoPic);

    Query<MProfile> query = DB.find(MProfile.class).fetch("picture").where().eq("name", name).query();

    // assert we get the profile with a null picture
    MProfile profile = query.findOne();
    assertNotNull(profile);

    String generatedSql = query.getGeneratedSql();
    assertSql(generatedSql).contains("select t0.id, t0.name, t1.type, t1.id, t1.url, t1.note from mprofile t0 left join mmedia t1 on t1.id = t0.picture_id and t1.type = 'Picture' where t0.name = ?");
    assertSql(generatedSql).contains("where t0.name = ?");
  }

}
