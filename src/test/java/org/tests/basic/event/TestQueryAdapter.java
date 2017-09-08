package org.tests.basic.event;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.basic.TOne;
import org.junit.Assert;
import org.junit.Test;

public class TestQueryAdapter extends BaseTestCase {

  @Test
  public void testSimple() {

    ResetBasicData.reset();

    TOne o = new TOne();
    o.setName("something");

    Ebean.save(o);

    //Ebean.find(TOne.class, o.getId());

    Query<TOne> queryFindId = Ebean.find(TOne.class)
      .setId(o.getId());

    TOne one = queryFindId.findOne();
    Assert.assertNotNull(one);
    Assert.assertEquals(one.getId(), o.getId());
    String generatedSql = queryFindId.getGeneratedSql();
    Assert.assertTrue(generatedSql.contains(" 1=1"));

  }
}
