package io.ebean.xtest.base;

import io.ebean.DB;
import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.QueryPlanInit;
import io.ebean.meta.QueryPlanRequest;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prototype: capture database query plans for native SQL DtoQuery.
 */
class DtoQueryPlanCaptureTest extends BaseTestCase {

  public static class DCustPlanCapture {
    Long id;
    String name;

    public void setId(Long id) {
      this.id = id;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  private List<DCustPlanCapture> runQuery() {
    return DB.getDefault()
      .findDto(DCustPlanCapture.class, "select id, name from o_customer where id > ?")
      .setParameter(0)
      .setLabel("custDtoPlan")
      .findList();
  }

  @Test
  void nativeDtoQuery_capturesQueryPlan() {
    ResetBasicData.reset();

    // build the DTO plan first (default threshold -> no capture yet)
    assertThat(runQuery()).isNotEmpty();

    // arming an already-built DTO plan should return its meta
    QueryPlanInit init = new QueryPlanInit();
    init.setAll(true);
    init.thresholdMicros(1);
    List<MetaQueryPlan> armed = DB.getDefault().metaInfo().queryPlanInit(init);
    assertThat(armed)
      .as("queryPlanInit includes the (already built) DTO plan")
      .anyMatch(p -> "dto.DCustPlanCapture.custDtoPlan".equals(p.label()));

    // run again now that the plan is armed -> bind values captured
    assertThat(runQuery()).isNotEmpty();

    QueryPlanRequest request = new QueryPlanRequest();
    request.maxCount(1000);
    request.maxTimeMillis(10_000);
    List<MetaQueryPlan> plans = DB.getDefault().metaInfo().queryPlanCollectNow(request);

    MetaQueryPlan dtoPlan = plans.stream()
      .filter(p -> "dto.DCustPlanCapture.custDtoPlan".equals(p.label()))
      .findFirst()
      .orElse(null);

    assertThat(dtoPlan).as("captured a native DTO query plan").isNotNull();
    assertThat(dtoPlan.sql()).contains("from o_customer where id > ?");
    assertThat(dtoPlan.plan()).isNotEmpty();
  }
}
