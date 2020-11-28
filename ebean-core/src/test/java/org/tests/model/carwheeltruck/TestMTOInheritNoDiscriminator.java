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
      .where().idEq(holder.getId())
      .findOne();

    assertThat(found).isNotNull();
    assertThat(found.getItems()).hasSize(2);
    assertThat(found.getItems()).extracting(TTruckHolderItem::getFoo).contains("a","b");

    final List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(1);
    if (isH2() || isPostgres()) {
      assertSql(sql.get(0)).contains("select t0.id, t0.name, t0.version, t2.type, t0.truck_plate_no, t0.basic_id, t1.id, t1.some_uid, t1.foo, t1.owner_id from ttruck_holder t0 join tcar t2 on t2.plate_no = t0.truck_plate_no left join ttruck_holder_item t1 on t1.owner_id = t0.id where t0.id = ?   order by t0.id");
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
