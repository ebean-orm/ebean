package org.tests.o2m.dm;


import io.ebean.annotation.DbJsonB;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
//@Table(name = "strategic_plans")
public class StrategicPlan extends HistoryColumns {
  private String name;

  @OneToMany(mappedBy = "strategicPlan", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<GoodsCapacity> goodsCapacities = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<GoodsCapacity> getGoodsCapacities() {
    return goodsCapacities;
  }

  public void setGoodsCapacities(List<GoodsCapacity> goodsCapacities) {
    this.goodsCapacities = goodsCapacities;
  }
}
