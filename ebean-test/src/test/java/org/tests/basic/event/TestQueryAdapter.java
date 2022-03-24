package org.tests.basic.event;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.QueryType;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
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

    DB.save(o);

    Query<TOne> queryFindId = DB.find(TOne.class).setId(o.getId());

    TOne one = queryFindId.findOne();

    assertThat(one.getId()).isEqualTo(o.getId());
    assertThat(sqlOf(queryFindId)).contains(" 1=1");

    Query<TOne> notUsedQuery = DB.find(TOne.class);
    assertThat(notUsedQuery.getQueryType()).isEqualTo(QueryType.FIND);

    LoggedSql.start();

    DB.update(TOne.class)
      .set("name", "mod")
      .where().idEq(o.getId())
      .update();

    List<String> sql = LoggedSql.collect();
    assertSql(sql.get(0)).contains(" 2=2");

    DB.find(TOne.class)
      .where().idEq(o.getId())
      .delete();

    sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains(" 3=3");

  }
}
