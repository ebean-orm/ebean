package org.tests.model.aggregation;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAggregationTopLevel extends BaseTestCase {

  @BeforeClass
  public static void setup() {
    loadData();
  }

  @Test
  public void query_noSelect() {

    Query<DMachineStatsAgg> query = Ebean.find(DMachineStatsAgg.class)
      .where().gt("date", LocalDate.now().minusDays(10))
      .query();

    List<DMachineStatsAgg> result = query.findList();
    assertThat(sqlOf(query)).contains("select t0.date, t0.machine_id from d_machine_stats t0 where t0.date > ?");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void query_machineTotalKms_withHaving() {

    Query<DMachineStatsAgg> query = Ebean.find(DMachineStatsAgg.class)
      .select("machine, date, totalKms, totalCost")
      .where().gt("date", LocalDate.now().minusDays(10))
      .having().gt("totalCost", 10)
      .query();

    List<DMachineStatsAgg> result = query.findList();
    assertThat(sqlOf(query)).contains("select t0.machine_id, t0.date, sum(t0.total_kms), sum(cost) from d_machine_stats t0 where t0.date > ?  group by t0.machine_id, t0.date having sum(cost) > ?");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void query_machineTotalKms() {

    Query<DMachineStatsAgg> query = Ebean.find(DMachineStatsAgg.class)
      .select("machine, totalKms, totalCost")
      .where().gt("date", LocalDate.now().minusDays(10))
      .query();

    List<DMachineStatsAgg> result = query.findList();
    assertThat(sqlOf(query)).contains("select t0.machine_id, sum(t0.total_kms), sum(cost) from d_machine_stats t0 where t0.date > ?  group by t0.machine_id");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void query_byDate() {

    Query<DMachineStatsAgg> query = Ebean.find(DMachineStatsAgg.class)
      .select("date, totalKms, hours, rate, totalCost, maxKms")
      .where().gt("date", LocalDate.now().minusDays(10))
      .having().gt("hours", 2)
      .query();

    List<DMachineStatsAgg> result = query.findList();
    assertThat(sqlOf(query)).contains("select t0.date, sum(t0.total_kms), sum(t0.hours), max(t0.rate), sum(cost), max(t0.total_kms) from d_machine_stats t0 where t0.date > ?  group by t0.date having sum(t0.hours) > ?");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void groupBy_date_dynamicFormula() {

    Query<DMachineStats> query = Ebean.find(DMachineStats.class)
      .select("date, sum(totalKms), sum(hours)")
      .where().gt("date", LocalDate.now().minusDays(10))
      .having().gt("sum(hours)", 2)
      .query();

    List<DMachineStats> result = query.findList();
    assertThat(sqlOf(query)).contains("select t0.date, sum(t0.total_kms), sum(t0.hours) from d_machine_stats t0 where t0.date > ?  group by t0.date having sum(t0.hours) > ?");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void groupBy_MachineAndDate_dynamicFormula() {

    Query<DMachineStats> query = Ebean.find(DMachineStats.class)
      .select("machine, date, max(rate)")
      .where().gt("date", LocalDate.now().minusDays(10))
      .query();

    List<DMachineStats> result = query.findList();
    assertThat(sqlOf(query)).contains("select t0.machine_id, t0.date, max(t0.rate) from d_machine_stats t0 where t0.date > ?  group by t0.machine_id, t0.date");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void groupBy_MachineWithJoin_dynamicFormula() {

    Query<DMachineStats> query = Ebean.find(DMachineStats.class)
      .select("max(rate), sum(totalKms)")
      .fetch("machine", "name")
      .where().gt("date", LocalDate.now().minusDays(10))
      .query();

    List<DMachineStats> result = query.findList();
    assertThat(sqlOf(query)).contains("select max(t0.rate), sum(t0.total_kms), t1.id, t1.name from d_machine_stats t0 left join dmachine t1 on t1.id = t0.machine_id  where t0.date > ?  group by t1.id, t1.name");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void groupBy_MachineDateWithJoin_dynamicFormula() {

    Query<DMachineStats> query = Ebean.find(DMachineStats.class)
      .select("machine, date, max(rate), sum(totalKms)")
      .fetch("machine", "name")
      .where().gt("date", LocalDate.now().minusDays(10))
      .query();

    List<DMachineStats> result = query.findList();
    assertThat(sqlOf(query)).contains("select t0.date, max(t0.rate), sum(t0.total_kms), t1.id, t1.name from d_machine_stats t0 left join dmachine t1 on t1.id = t0.machine_id  where t0.date > ?  group by t0.date, t1.id, t1.name");
    assertThat(result).isNotEmpty();
  }

  private static void loadData() {

    List<DMachine> machines = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
      machines.add(new DMachine("Machine"+i));
    }

    Ebean.saveAll(machines);

    List<DMachineStats> allStats = new ArrayList<>();

    LocalDate date = LocalDate.now();
    for (int i = 0; i < 8; i++) {
      for (DMachine machine : machines) {

        DMachineStats stats = new DMachineStats(machine, date);

        stats.setHours(i * 4);
        stats.setTotalKms(i * 100);
        stats.setCost(BigDecimal.valueOf(i * 50));
        stats.setRate(BigDecimal.valueOf(i * 2));

        allStats.add(stats);
      }

      date = date.minusDays(1);
    }

    Ebean.saveAll(allStats);
  }
}
