package io.ebean.xtest.base;

import io.ebean.DB;
import io.ebean.meta.MetaQueryPlan;
import io.ebean.meta.QueryPlanInit;
import io.ebean.meta.QueryPlanRequest;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
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

  public static class DCustOrmBacked {
    Long id;
    String name;

    public void setId(Long id) {
      this.id = id;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  private List<DCustPlanCapture> runNative() {
    return DB.getDefault()
      .findDto(DCustPlanCapture.class, "select id, name from o_customer where id > ?")
      .setParameter(0)
      .setLabel("custDtoPlan")
      .findList();
  }

  private List<DCustOrmBacked> runOrmBacked() {
    return DB.getDefault()
      .find(Customer.class)
      .select("id, name")
      .asDto(DCustOrmBacked.class)
      .setLabel("custOrmBacked")
      .findList();
  }

  @Test
  void nativeDtoQuery_capturesQueryPlan() {
    ResetBasicData.reset();

    // build the plans first (default threshold -> no capture yet)
    assertThat(runNative()).isNotEmpty();
    runOrmBacked();

    // arming should include the native DTO plan but NOT the ORM-backed DTO plan
    QueryPlanInit init = new QueryPlanInit();
    init.setAll(true);
    init.thresholdMicros(1);
    List<MetaQueryPlan> armed = DB.getDefault().metaInfo().queryPlanInit(init);

    assertThat(armed)
      .as("native DTO plan is armed")
      .anyMatch(p -> "dto.DCustPlanCapture.custDtoPlan".equals(p.label()));
    assertThat(armed)
      .as("ORM-backed DTO plan is not armed via the DTO path")
      .noneMatch(p -> "dto.DCustOrmBacked.custOrmBacked".equals(p.label()));
    assertThat(armed)
      .as("ORM-backed DTO captures via the underlying ORM query plan instead")
      .anyMatch(p -> "orm.Customer.custOrmBacked".equals(p.label()));

    // run again now that the native plan is armed -> bind values captured
    assertThat(runNative()).isNotEmpty();

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
