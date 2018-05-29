package org.tests.merge;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.MergeOptions;
import io.ebean.MergeOptionsBuilder;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMergeM2M extends BaseTestCase {

  @Test
  public void m2mMerge() {

    MGroup group1 = new MGroup(1, "gone");
    MGroup group2 = new MGroup(2, "gtwo");
    MGroup group3 = new MGroup(3, "gthree");
    MGroup group4 = new MGroup(4, "gfour");
    MGroup group5 = new MGroup(5, "gfive");

    Ebean.save(group1);
    Ebean.save(group2);
    Ebean.save(group3);
    Ebean.save(group4);
    Ebean.save(group5);

    MMachine machine = new MMachine("mac1");
    machine.getGroups().add(group1);
    machine.getGroups().add(group2);
    machine.getGroups().add(group3);

    MergeOptions options = new MergeOptionsBuilder().addPath("groups").build();

    LoggedSqlCollector.start();

    Ebean.merge(machine, options);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(3);
    assertThat(sql.get(0)).contains("select");
    assertThat(sql.get(1)).contains("insert into mmachine");
    assertThat(sql.get(2)).contains("insert into mmachine_mgroup");

    machine.setName("mac1-mod");
    machine.getGroups().remove(group2);
    machine.getGroups().remove(group3);
    machine.getGroups().add(group4);
    machine.getGroups().add(group5);

    Ebean.merge(machine, options);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(4);
    assertThat(sql.get(0)).contains("select");
    assertThat(sql.get(1)).contains("delete from mmachine_mgroup");
    assertThat(sql.get(2)).contains("insert into mmachine_mgroup");
    assertThat(sql.get(3)).contains("update mmachine");

  }
}
