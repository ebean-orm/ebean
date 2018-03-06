package org.tests.basic.event;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.QueryType;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.basic.TOne;

import java.util.List;

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

    Query<TOne> notUsedQuery = Ebean.find(TOne.class);
    assertThat(notUsedQuery.getQueryType()).isEqualTo(QueryType.FIND);

    LoggedSqlCollector.start();

    Ebean.update(TOne.class)
      .set("name", "mod")
      .where().idEq(o.getId())
      .update();

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql.get(0)).contains(" 2=2");

    Ebean.find(TOne.class)
      .where().idEq(o.getId())
      .delete();

    sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains(" 3=3");

  }
}
