package org.tests.o2m.dm;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
//@Table(name = "strategic_plan_goods_capacities")
public class GoodsCapacity  {
  @Id
  private Long id;

  @ManyToOne
  private StrategicPlan strategicPlan;
  @ManyToOne
  private GoodsEntity goods;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public StrategicPlan getStrategicPlan() {
    return strategicPlan;
  }

  public void setStrategicPlan(StrategicPlan strategicPlan) {
    this.strategicPlan = strategicPlan;
  }

  public GoodsEntity getGoods() {
    return goods;
  }

  public void setGoods(GoodsEntity goods) {
    this.goods = goods;
  }
}
