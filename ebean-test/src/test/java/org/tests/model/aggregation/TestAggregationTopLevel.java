package org.tests.model.aggregation;

import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAggregationTopLevel extends BaseTestCase {

  @BeforeAll
  public static void setup() {
    loadData();
  }

  @Test
  public void query_noSelect() {

    Query<DMachineStatsAgg> query = DB.find(DMachineStatsAgg.class)
      .where().gt("date", LocalDate.now().minusDays(10))
      .query();

    List<DMachineStatsAgg> result = query.findList();
    assertThat(sqlOf(query)).contains("select t0.machine_id, t0.edate from d_machine_stats t0 where t0.edate > ?");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void query_count() {

    Query<DMachineStatsAgg> query = DB.find(DMachineStatsAgg.class)
      .select("date, totalKms")
      .setMaxRows(10)
      .having().gt("totalKms", 1)
      .query();

    LoggedSql.start();
    query.findCount();
    query.findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);

    if (isH2() || isPostgresCompatible()) {
      assertSql(sql.get(0)).contains("select count(*) from ( select t0.edate, sum(t0.total_kms) from d_machine_stats t0 group by t0.edate having sum(t0.total_kms) > ?)");
      assertSql(sql.get(1)).contains("select t0.edate, sum(t0.total_kms) from d_machine_stats t0 group by t0.edate having sum(t0.total_kms) > ? limit 10");
    } else if (isSqlServer()) {
      assertSql(sql.get(0)).contains("select count(*) from ( select t0.edate, sum(t0.total_kms) from d_machine_stats t0 group by t0.edate having sum(t0.total_kms) > ?) as c");
    }
  }

  @Test
  public void query_machineTotalKms_withHaving() {

    Query<DMachineStatsAgg> query = DB.find(DMachineStatsAgg.class)
      .select("machine, date, totalKms, totalCost")
      .where().gt("date", LocalDate.now().minusDays(10))
      .having().gt("totalCost", 10)
      .query();

    List<DMachineStatsAgg> result = query.findList();
    assertThat(sqlOf(query)).contains("select t0.machine_id, t0.edate, sum(t0.total_kms), sum(cost) from d_machine_stats t0 where t0.edate > ? group by t0.machine_id, t0.edate having sum(cost) > ?");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void query_machineTotalKms() {

    Query<DMachineStatsAgg> query = DB.find(DMachineStatsAgg.class)
      .select("machine, totalKms, totalCost")
      .where().gt("date", LocalDate.now().minusDays(10))
      .query();

    List<DMachineStatsAgg> result = query.findList();
    assertThat(sqlOf(query)).contains("select t0.machine_id, sum(t0.total_kms), sum(cost) from d_machine_stats t0 where t0.edate > ? group by t0.machine_id");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void query_byDate() {

    Query<DMachineStatsAgg> query = DB.find(DMachineStatsAgg.class)
      .select("date, totalKms, hours, rate, totalCost, maxKms")
      .where().gt("date", LocalDate.now().minusDays(10))
      .having().gt("hours", 2)
      .query();

    List<DMachineStatsAgg> result = query.findList();
    assertThat(sqlOf(query)).contains("select t0.edate, sum(t0.total_kms), sum(t0.hours), max(t0.rate), sum(cost), max(t0.total_kms) from d_machine_stats t0 where t0.edate > ? group by t0.edate having sum(t0.hours) > ?");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void groupBy_machineDate_dynamicFormula() {

    Query<DMachineStats> query = DB.find(DMachineStats.class)
      .select("machine, date, sum(totalKms), sum(hours)")
      .where().gt("date", LocalDate.now().minusDays(10))
      .having().gt("sum(hours)", 2)
      .query();

    List<DMachineStats> result = query.findList();
    assertThat(sqlOf(query)).contains("select t0.machine_id, t0.edate, sum(t0.total_kms), sum(t0.hours) from d_machine_stats t0 where t0.edate > ? group by t0.machine_id, t0.edate having sum(t0.hours) > ?");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void groupBy_machine_dynamicFormula() {

    Query<DMachineStats> query = DB.find(DMachineStats.class)
      .select("machine, sum(totalKms)")
      .where().gt("date", LocalDate.now().minusDays(10))
      .having().gt("sum(hours)", 2)
      .query();

    List<DMachineStats> result = query.findList();
    assertThat(sqlOf(query)).contains("select t0.machine_id, sum(t0.total_kms) from d_machine_stats t0 where t0.edate > ? group by t0.machine_id having sum(t0.hours) > ?");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void groupBy_machine_dynamicFormula_withJoin() {

    Query<DMachineStats> query = DB.find(DMachineStats.class)
      .select("sum(totalKms), sum(hours)")
      .fetch("machine", "name")
      .where().gt("date", LocalDate.now().minusDays(10))
      .having().gt("sum(hours)", 2)
      .query();

    LoggedSql.start();

    List<DMachineStats> result = query.findList();
    assertThat(result).isNotEmpty();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("select sum(t0.total_kms), sum(t0.hours), t1.id, t1.name from d_machine_stats t0 join dmachine t1 on t1.id = t0.machine_id where t0.edate > ? group by t1.id, t1.name having sum(t0.hours) > ?");
  }

  @Test
  public void groupBy_machine_dynamicFormula_withJoin2() {

    Query<DMachineStats> query = DB.find(DMachineStats.class)
      .select("date, sum(totalKms), sum(hours)")
      .fetch("machine", "name")
      .where().gt("date", LocalDate.now().minusDays(10))
      .having().gt("sum(hours)", 2)
      .query();

    LoggedSql.start();

    List<DMachineStats> result = query.findList();
    assertThat(result).isNotEmpty();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("select t0.edate, sum(t0.total_kms), sum(t0.hours), t1.id, t1.name from d_machine_stats t0 join dmachine t1 on t1.id = t0.machine_id where t0.edate > ? group by t0.edate, t1.id, t1.name having sum(t0.hours) > ?");
  }


  @Test
  public void groupBy_machine_dynamicFormula_withQueryJoin() {

    Query<DMachineStats> query = DB.find(DMachineStats.class)
      .select("sum(totalKms), sum(hours)")
      .fetchQuery("machine", "name")
      .where().gt("date", LocalDate.now().minusDays(10))
      .having().gt("sum(hours)", 2)
      .query();

    LoggedSql.start();

    List<DMachineStats> result = query.findList();
    assertThat(result).isNotEmpty();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);

    assertSql(sql.get(0)).contains("select sum(t0.total_kms), sum(t0.hours), t0.machine_id from d_machine_stats t0 where t0.edate > ? group by t0.machine_id having sum(t0.hours) > ?");
    assertSql(sql.get(1)).contains("select t0.id, t0.name from dmachine t0 where t0.id");
  }

  @Test
  public void groupBy_machine_dynamicFormula_withQueryJoin2() {

    Query<DMachineStats> query = DB.find(DMachineStats.class)
      .select("date, sum(totalKms), sum(hours)")
      .fetchQuery("machine", "name")
      .where().gt("date", LocalDate.now().minusDays(10))
      .having().gt("sum(hours)", 2)
      .query();

    LoggedSql.start();

    List<DMachineStats> result = query.findList();
    assertThat(result).isNotEmpty();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);

    assertSql(sql.get(0)).contains("select t0.edate, sum(t0.total_kms), sum(t0.hours), t0.machine_id from d_machine_stats t0 where t0.edate > ? group by t0.edate, t0.machine_id having sum(t0.hours) > ?");
    assertSql(sql.get(1)).contains("select t0.id, t0.name from dmachine t0 where t0.id");
  }

  @Test
  public void groupBy_MachineAndDate_dynamicFormula() {

    Query<DMachineStats> query = DB.find(DMachineStats.class)
      .select("machine, date, max(rate)")
      .where().gt("date", LocalDate.now().minusDays(10))
      .query();

    List<DMachineStats> result = query.findList();
    assertThat(sqlOf(query)).contains("select t0.machine_id, t0.edate, max(t0.rate) from d_machine_stats t0 where t0.edate > ? group by t0.machine_id, t0.edate");
    assertThat(result).isNotEmpty();
  }

  @Test
  public void groupBy_in_fetchClause_singleRowInMany() {

    LoggedSql.start();

    Query<DMachine> query = DB.find(DMachine.class)
      .select("name")
      .fetch("machineStats", "sum(totalKms)")
      .where().eq("name", "Machine0")
      .query();

    List<DMachine> result = query.findList();
    assertThat(result).isNotEmpty();
    assertThat(result.get(0).getMachineStats().get(0).getTotalKms()).isNotNull();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);

    assertThat(sqlOf(query)).contains("select t0.id, t0.name, sum(t1.total_kms) from dmachine t0 left join d_machine_stats t1 on t1.machine_id = t0.id where t0.name = ? group by t0.id, t0.name order by t0.id");
  }

  @Test
  public void groupBy_in_fetchClause_multipleRowsInMany() {

    LoggedSql.start();

    Query<DMachine> query = DB.find(DMachine.class)
      .select("name")
      .fetch("machineStats", "date, max(rate), sum(totalKms)")
      .where().eq("name", "Machine0")
      .query();

    List<DMachine> result = query.findList();
    assertThat(result).isNotEmpty();
    assertThat(result.get(0).getMachineStats().size()).isGreaterThan(1); // expect 8 as grouped by date

    DMachineStats firstStat = result.get(0).getMachineStats().get(0);
    assertThat(firstStat.getTotalKms()).isNotNull();
    assertThat(firstStat.getRate()).isNotNull();
    assertThat(firstStat.getDate()).isNotNull();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);

    assertThat(sqlOf(query)).contains("select t0.id, t0.name, t1.edate, max(t1.rate), sum(t1.total_kms) from dmachine t0 left join d_machine_stats t1 on t1.machine_id = t0.id where t0.name = ? group by t0.id, t0.name, t1.edate order by t0.id");
  }

  public static void loadData() {

    List<DMachine> machines = new ArrayList<>();
    DOrg org = new DOrg("other");
    org.save();

    for (int i = 0; i < 5; i++) {
      machines.add(new DMachine(org, "Machine" + i));
    }

    DB.saveAll(machines);

    List<DMachineStats> allStats = new ArrayList<>();

    LocalDate date = LocalDate.now();
    for (int i = 0; i < 8; i++) {
      for (DMachine machine : machines) {

        DMachineStats stats = new DMachineStats(machine, date);

        long offset = i * machine.getId();
        stats.setHours(offset * 4);
        stats.setTotalKms(offset * 100);
        stats.setCost(BigDecimal.valueOf(offset * 50));
        stats.setRate(BigDecimal.valueOf(offset * 2));

        allStats.add(stats);
      }

      date = date.minusDays(1);
    }

    DB.saveAll(allStats);
  }
}
