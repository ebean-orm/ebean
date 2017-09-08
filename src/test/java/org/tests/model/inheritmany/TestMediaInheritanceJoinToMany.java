package org.tests.model.inheritmany;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMediaInheritanceJoinToMany extends BaseTestCase {

  @Test
  public void test() {


    String name = "nopic" + new Random().nextInt();

    MProfile profileWithNoPic = new MProfile();
    profileWithNoPic.setName(name);

    Ebean.save(profileWithNoPic);

    Query<MProfile> query = Ebean.find(MProfile.class).fetch("picture").where().eq("name", name).query();

    // assert we get the profile with a null picture
    MProfile profile = query.findOne();
    Assert.assertNotNull(profile);

    // select t0.id c0, t0.name c1, t1.type c2, t1.id c3, t1.url c4, t1.note c5
    // from profile t0
    // left  join media t1 on t1.id = t0.picture_id and t1.type = 'Picture'
    // where t0.name = ? ; --bind(nopic)

    // specifically t1.type = 'Picture' ... on on the join and not in the where

    String generatedSql = query.getGeneratedSql();
    assertThat(generatedSql).contains("from mprofile t0 left join mmedia t1 on t1.id = t0.picture_id and t1.type = 'Picture' ");
    assertThat(generatedSql).contains("where t0.name = ? ");

  }

}
