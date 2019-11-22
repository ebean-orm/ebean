package org.tests.model.carwheeltruck;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMTOInheritNoDiscriminator extends BaseTestCase {

  @Test
  public void test() {

    final TTruckHolder holder = setup();

    LoggedSqlCollector.start();

    final TTruckHolder found = DB.find(TTruckHolder.class)
      .fetch("items")
      .fetch("truck","*")
      .where().idEq(holder.getId())
      .findOne();

    assertThat(found).isNotNull();
    assertThat(found.getItems()).hasSize(2);
    assertThat(found.getItems()).extracting(TTruckHolderItem::getFoo).contains("a","b");
    assertThat(found.getTruck()).isInstanceOf(TTruck.class);


    final List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(1);
    if (isH2() || isPostgres()) {
      assertThat(sql.get(0)).contains("t2.truckload");
      assertThat(sql.get(0)).doesNotContain("t2.type"); // not required here
    }
  }

  private TTruckHolder setup() {
    TTruck truck = new TTruck();
    truck.setPlateNo("J78");
    truck.setLoad(43L);

    DB.save(truck);

    TTruckHolder holder = new TTruckHolder("h1", truck);
    holder.getItems().add(new TTruckHolderItem("a"));
    holder.getItems().add(new TTruckHolderItem("b"));

    DB.save(holder);

    return holder;
  }
}
