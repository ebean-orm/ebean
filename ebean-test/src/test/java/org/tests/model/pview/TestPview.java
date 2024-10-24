package org.tests.model.pview;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPview extends BaseTestCase {

  @Test
  public void test() {

    Wview wview = DB.reference(Wview.class, UUID.randomUUID());

    Query<Paggview> query = DB.find(Paggview.class);
    query.select("amount");
    query.where().eq("pview.wviews", wview);
    query.orderBy("pview.value");
    query.findList();
    String generatedSql = sqlOf(query, 1);

    assertThat(generatedSql).contains("select distinct t0.amount, t1.value from paggview t0 join pp u1 on u1.id = t0.pview_id join pp_to_ww u2z_ on u2z_.pp_id = u1.id join wview u2 on u2.id = u2z_.ww_id left join pp t1 on t1.id = t0.pview_id where u2.id = ? order by t1.value");

  }

}
