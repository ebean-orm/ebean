package org.tests.o2m;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.o2m.dm.*;

import java.sql.Timestamp;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class OneToManyListMarkAsDirtyTest extends BaseTestCase {
  @Test
  public void workingTestCase() {
    var g = new GoodsEntity();
    DB.save(g);

    var planStart = new StrategicPlan();
    planStart.setName("aaa");
    planStart.setGoodsCapacities(new ArrayList<>());
    DB.save(planStart);

    var planFirstCapacity = DB.find(StrategicPlan.class, planStart.getId());
    planFirstCapacity.setGoodsCapacities(new ArrayList<>());
    var a_b = new GoodsCapacity();
    a_b.setGoods(g);
    planFirstCapacity.getGoodsCapacities().add(a_b);
    DB.save(planFirstCapacity);

    var planSecondCapacity = DB.find(StrategicPlan.class, planStart.getId());
    planSecondCapacity.setGoodsCapacities(new ArrayList<>());
    var a_c = new GoodsCapacity();
    a_c.setGoods(g);
    planSecondCapacity.getGoodsCapacities().add(a_c);
    // without this the goods capacity is saved correctly, version remains same
    // (not sure if version should stay the same if OneToMany relationship is changed (through cascade)
    // DB.markAsDirty(planSecondCapacity);
    DB.save(planSecondCapacity);

    var refreshedPlanSecondCapacity = DB.find(StrategicPlan.class, planStart.getId());
    assertThat(refreshedPlanSecondCapacity.getGoodsCapacities().size()).isEqualTo(1);
    assertThat(refreshedPlanSecondCapacity.getGoodsCapacities().get(0).getId()).isEqualTo(a_c.getId());
    assertThat(refreshedPlanSecondCapacity.getVersion()).isEqualTo(planFirstCapacity.getVersion());
    assertThat(refreshedPlanSecondCapacity.getWhenModified()).isEqualTo(planFirstCapacity.getWhenModified());
  }

  @Test
  public void oneToManyShouldBeDeletedWithParentMarkAsDirtyTest() {
    var g = new GoodsEntity();
    DB.save(g);

    var planStart = new StrategicPlan();
    planStart.setName("aaa");
    planStart.setGoodsCapacities(new ArrayList<>());
    DB.save(planStart);

    var planFirstCapacity = DB.find(StrategicPlan.class, planStart.getId());
    planFirstCapacity.setGoodsCapacities(new ArrayList<>());
    var a_b = new GoodsCapacity();
    a_b.setGoods(g);
    planFirstCapacity.getGoodsCapacities().add(a_b);
    DB.save(planFirstCapacity);

    var planSecondCapacity = DB.find(StrategicPlan.class, planStart.getId());
    planSecondCapacity.setGoodsCapacities(new ArrayList<>());
    var a_c = new GoodsCapacity();
    a_c.setGoods(g);
    planSecondCapacity.getGoodsCapacities().add(a_c);
    // force version increase
    // without this the goods capacity is saved correctly, but version isn't
    DB.markAsDirty(planSecondCapacity);
    DB.save(planSecondCapacity);

    var refreshedPlanSecondCapacity = DB.find(StrategicPlan.class, planStart.getId());
    assertThat(refreshedPlanSecondCapacity.getGoodsCapacities().size()).isEqualTo(1);
    assertThat(refreshedPlanSecondCapacity.getGoodsCapacities().get(0).getId()).isEqualTo(a_c.getId());
    assertThat(refreshedPlanSecondCapacity.getVersion()).isGreaterThan(planFirstCapacity.getVersion());
    assertThat(refreshedPlanSecondCapacity.getWhenModified()).isAfter(planFirstCapacity.getWhenModified());
  }

  @Test
  public void oneToManyShouldBeDeletedWithParentMarkAsDirtyTestWorkAround() {
    var g = new GoodsEntity();
    DB.save(g);

    var planStart = new StrategicPlan();
    planStart.setName("aaa");
    planStart.setGoodsCapacities(new ArrayList<>());
    DB.save(planStart);

    var planFirstCapacity = DB.find(StrategicPlan.class, planStart.getId());
    planFirstCapacity.setGoodsCapacities(new ArrayList<>());
    var a_b = new GoodsCapacity();
    a_b.setGoods(g);
    planFirstCapacity.getGoodsCapacities().add(a_b);
    DB.save(planFirstCapacity);

    var planSecondCapacity = DB.find(StrategicPlan.class, planStart.getId());
//    planSecondCapacity.setGoodsCapacities(new ArrayList<>());
    planSecondCapacity.getGoodsCapacities().clear();
    var a_c = new GoodsCapacity();
    a_c.setGoods(g);
    planSecondCapacity.getGoodsCapacities().add(a_c);
    // force version increase
    // without this the goods capacity is saved correctly, but version isn't
    DB.markAsDirty(planSecondCapacity);
    DB.save(planSecondCapacity);

    var refreshedPlanSecondCapacity = DB.find(StrategicPlan.class, planStart.getId());
    assertThat(refreshedPlanSecondCapacity.getGoodsCapacities().size()).isEqualTo(1);
    assertThat(refreshedPlanSecondCapacity.getGoodsCapacities().get(0).getId()).isEqualTo(a_c.getId());
    assertThat(refreshedPlanSecondCapacity.getVersion()).isGreaterThan(planFirstCapacity.getVersion());
    assertThat(refreshedPlanSecondCapacity.getWhenModified()).isAfter(planFirstCapacity.getWhenModified());
  }

  @Test
  public void oneToManyShouldBeDeletedWithParentManualModifiedWhenTest() {
    var g = new GoodsEntity();
    DB.save(g);

    var planStart = new StrategicPlan();
    planStart.setName("aaa");
    planStart.setGoodsCapacities(new ArrayList<>());
    DB.save(planStart);

    var planFirstCapacity = DB.find(StrategicPlan.class, planStart.getId());
    planFirstCapacity.setGoodsCapacities(new ArrayList<>());
    var a_b = new GoodsCapacity();
    a_b.setGoods(g);
    planFirstCapacity.getGoodsCapacities().add(a_b);
    DB.save(planFirstCapacity);

    var planSecondCapacity = DB.find(StrategicPlan.class, planStart.getId());
    planSecondCapacity.setGoodsCapacities(new ArrayList<>());
    var a_c = new GoodsCapacity();
    a_c.setGoods(g);
    planSecondCapacity.getGoodsCapacities().add(a_c);
    planSecondCapacity.setWhenCreated(new Timestamp(System.currentTimeMillis()));

    DB.save(planSecondCapacity);

    var refreshedPlanSecondCapacity = DB.find(StrategicPlan.class, planStart.getId());
    assertThat(refreshedPlanSecondCapacity.getGoodsCapacities().size()).isEqualTo(1);
    assertThat(refreshedPlanSecondCapacity.getGoodsCapacities().get(0).getId()).isEqualTo(a_c.getId());
    assertThat(refreshedPlanSecondCapacity.getVersion()).isGreaterThan(planFirstCapacity.getVersion());
    assertThat(refreshedPlanSecondCapacity.getWhenModified()).isAfter(planFirstCapacity.getWhenModified());
  }
}
