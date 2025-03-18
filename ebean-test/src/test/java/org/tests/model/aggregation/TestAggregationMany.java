package org.tests.model.aggregation;

import io.ebean.LazyInitialisationException;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestAggregationMany extends BaseTestCase {

  @Test
  public void fetchQuery_toAggregate() {

    DOrg org = MachineUseData.load();

    LoggedSql.start();

    List<DMachine> machines = DB.find(DMachine.class)
      .setDisableLazyLoading(true)
      .fetchQuery("auxUseAggs", "name, useSecs, fuel")
      .where().eq("organisation", org)
      .findList();

    for (DMachine machine : machines) {
      assertThat(machine.getAuxUseAggs()).isNotEmpty();
      assertThrows(LazyInitialisationException.class, machine::getMachineStats);
    }

    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select t0.id, t0.name, t0.version, t0.organisation_id from dmachine t0 where t0.organisation_id = ?");

    assertThat(machines).hasSize(5);
    if (isH2()) {
      assertSql(sql.get(1)).contains("select t0.machine_id, t0.name, sum(t0.use_secs), sum(t0.fuel) from d_machine_aux_use t0 where (t0.machine_id) in (?,?,?,?,?) group by t0.machine_id, t0.name;");
    }
  }


  @Test
  public void fetch_toAggregate() {

    DOrg org = MachineUseData.load();

    LoggedSql.start();

    List<DMachine> machines = DB.find(DMachine.class)
      .setDisableLazyLoading(true)
      .select("name")
      .fetch("auxUseAggs", "name, useSecs, fuel")
      .where().eq("organisation", org)
      .findList();

    for (DMachine machine : machines) {
      assertThat(machine.getAuxUseAggs()).isNotEmpty();
      assertThrows(LazyInitialisationException.class, machine::getMachineStats);
    }

    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("select t0.id, t0.name, t1.name, sum(t1.use_secs), sum(t1.fuel) from dmachine t0 left join d_machine_aux_use t1 on t1.machine_id = t0.id where t0.organisation_id = ? group by t0.id, t0.name, t1.name order by t0.id");
  }

  @Test
  public void fetch_toAggregate_onlyAggColumnsInFetchProperties() {

    DOrg org = MachineUseData.load();

    LoggedSql.start();

    List<DMachine> machines = DB.find(DMachine.class)
      .setDisableLazyLoading(true)
      .select("name")
      .fetch("auxUseAggs", "useSecs, fuel")
      .where().eq("organisation", org)
      .findList();

    for (DMachine machine : machines) {
      assertThat(machine.getAuxUseAggs()).isNotEmpty();
      System.out.println(machine);
      assertThrows(LazyInitialisationException.class, machine::getMachineStats);
    }

    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("select t0.id, t0.name, sum(t1.use_secs), sum(t1.fuel) from dmachine t0 left join d_machine_aux_use t1 on t1.machine_id = t0.id where t0.organisation_id = ? group by t0.id, t0.name order by t0.id");
  }
}
