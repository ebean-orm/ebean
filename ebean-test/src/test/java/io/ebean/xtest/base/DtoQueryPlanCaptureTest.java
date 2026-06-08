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

  @Test
  void nativeDtoQuery_capturesQueryPlan() {
    ResetBasicData.reset();

    // arm bind capture with a very low threshold so new (DTO) plans capture
    QueryPlanInit init = new QueryPlanInit();
    init.setAll(true);
    init.thresholdMicros(1);
    DB.getDefault().metaInfo().queryPlanInit(init);

    // native SQL DtoQuery - creates the DTO plan with the low threshold and captures bind values
    for (int i = 0; i < 3; i++) {
      List<DCustPlanCapture> list = DB.getDefault()
        .findDto(DCustPlanCapture.class, "select id, name from o_customer where id > ?")
        .setParameter(0)
        .setLabel("custDtoPlan")
        .findList();
      assertThat(list).isNotEmpty();
    }

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
