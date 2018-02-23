package org.tests.basic.event;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.basic.TOne;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryAdapter extends BaseTestCase {

  @Test
  public void testSimple() {

    ResetBasicData.reset();

    TOne o = new TOne();
    o.setName("something");

    Ebean.save(o);

    Query<TOne> queryFindId = Ebean.find(TOne.class).setId(o.getId());

    TOne one = queryFindId.findOne();

    assertThat(one.getId()).isEqualTo(o.getId());
    assertThat(sqlOf(queryFindId)).contains(" 1=1");
  }
}
