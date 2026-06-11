package io.ebean.xtest.base;

import io.ebean.DB;
import io.ebean.SqlRow;
import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.QueryPlanInit;
import io.ebean.meta.QueryPlanRequest;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prototype: capture database query plans for native SQL SqlQuery.
 */
class SqlQueryPlanCaptureTest extends BaseTestCase {

  private List<SqlRow> runQuery() {
    return DB.getDefault()
      .sqlQuery("select id, name from o_customer where id > ? and 'something' = 'something' ")
      .setParameter(0)
      .setLabel("custSqlPlan")
      .findList();
  }

  @Test
  void sqlQuery_capturesQueryPlan() {
    ResetBasicData.reset();

    // build the plan first (default threshold -> no capture yet)
    assertThat(runQuery()).isNotEmpty();

    // arming an already-built SqlQuery plan should return its meta
    QueryPlanInit init = new QueryPlanInit();
    init.setAll(true);
    init.thresholdMicros(1);
    List<MetaQueryPlan> armed = DB.getDefault().metaInfo().queryPlanInit(init);
    assertThat(armed)
      .as("queryPlanInit includes the (already built) SqlQuery plan")
      .anyMatch(p -> "sql.query.custSqlPlan".equals(p.label()));

    // run again now that the plan is armed -> bind values captured
    assertThat(runQuery()).isNotEmpty();

    QueryPlanRequest request = new QueryPlanRequest();
    request.maxCount(1000);
    request.maxTimeMillis(10_000);
    List<MetaQueryPlan> plans = DB.getDefault().metaInfo().queryPlanCollectNow(request);

    MetaQueryPlan sqlPlan = plans.stream()
      .filter(p -> "sql.query.custSqlPlan".equals(p.label()))
      .findFirst()
      .orElse(null);

    assertThat(sqlPlan).as("captured a SqlQuery query plan").isNotNull();
    assertThat(sqlPlan.sql()).contains("from o_customer where id > ?");
    assertThat(sqlPlan.plan()).isNotEmpty();
  }
}
