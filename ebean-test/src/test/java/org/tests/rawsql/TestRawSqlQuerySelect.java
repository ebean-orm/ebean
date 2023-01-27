package org.tests.rawsql;

import io.ebean.DB;
import io.ebean.FetchGroup;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ResetBasicData;
import org.tests.rawsql.transport.QuerySumResponse;
import org.tests.rawsql.transport.SampleReport;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestRawSqlQuerySelect extends BaseTestCase {

  @Test
  public void testRawSQL() {

    ResetBasicData.reset();

    RawSql rawSql = RawSqlBuilder.parse("select id, name, anniversary, city from (select c.id, c.name, c.anniversary, a.city from o_customer c left join o_address a ON a.id = c.billing_address_id order by c.id) w").create();

    var query = DB.find(SampleReport.class);
    query.setMaxRows(10);

    List<SampleReport> list = query.findList();
    assertNotNull(list);

    String sumSql = "COUNT(1)::Long AS count, SUM(id) AS sum";

    query.setRawSql(rawSql);
    //query.select(sumSql);
    query.select(FetchGroup.of(SampleReport.class, sumSql));

    query.asDto(QuerySumResponse.class);
    var countSum = query.findOneOrEmpty().orElseThrow();
    //assertThat((countSum instanceof QuerySumResponse));
  }
}
