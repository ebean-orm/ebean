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

    MMachine m0 = new MMachine("mac1");
    m0.getGroups().add(group1);
    m0.getGroups().add(group2);
    m0.getGroups().add(group3);

    MergeOptions options = new MergeOptionsBuilder().addPath("groups").build();

    LoggedSqlCollector.start();

    Ebean.merge(m0, options);

    List<String> sql = LoggedSqlCollector.current();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(6);
      assertThat(sql.get(0)).contains("select");
      assertThat(sql.get(1)).contains("insert into mmachine");
      assertThat(sql.get(2)).contains("insert into mmachine_mgroup");
      assertSqlBind(sql, 3, 5);
    } else {
      assertThat(sql).hasSize(5);
      assertThat(sql.get(0)).contains("select");
      assertThat(sql.get(1)).contains("insert into mmachine");
      assertThat(sql.get(2)).contains("insert into mmachine_mgroup");
      assertThat(sql.get(3)).contains("insert into mmachine_mgroup");
      assertThat(sql.get(4)).contains("insert into mmachine_mgroup");
    }

    MMachine m1 = new MMachine("mac1");
    m1.setId(m0.getId());
    m1.setName("mac1-mod");
    m1.getGroups().remove(group2);
    m1.getGroups().remove(group3);
    m1.getGroups().add(group4);
    m1.getGroups().add(group5);

    Ebean.merge(m1, options);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(9);
    assertThat(sql.get(0)).contains("select");
    assertThat(sql.get(1)).contains("delete from mmachine_mgroup");
    assertSqlBind(sql, 2, 4);

    assertThat(sql.get(5)).contains("insert into mmachine_mgroup");
    assertSqlBind(sql, 6, 7);
    assertThat(sql.get(8)).contains("update mmachine");
  }
}
