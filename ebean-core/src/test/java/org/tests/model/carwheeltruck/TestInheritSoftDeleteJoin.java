package org.tests.model.carwheeltruck;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebeantest.LoggedSql;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInheritSoftDeleteJoin extends BaseTestCase {

  @Test
  public void expect_space() {

    TTruck truck = new TTruck();
    truck.setPlateNo("MT998");
    truck.setLoad(43L);

    DB.save(truck);

    TTruckHolder holder = new TTruckHolder("MTH", truck);
    DB.save(holder);

    LoggedSql.start();
    final TTruckHolder found = DB.find(TTruckHolder.class)
      .fetch("truck")
      .where().idEq(holder.getId())
      .findOne();

    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains(" and t1.type = 'truck' and t1.deleted = ");
    assertThat(found).isNotNull();
  }
}
