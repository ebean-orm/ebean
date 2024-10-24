package org.tests.inheritance.bothsides;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class TestInheritanceBothSides extends BaseTestCase {

  @Test
  public void selectSourceBaseSql() {

    final Query<SourceBase> query = DB.find(SourceBase.class).orderBy("pos");
    query.findList();

    assertThat(sqlOf(query)).contains("select t0.dtype, t0.id, t0.name, t0.pos, t1.dtype, t0.target_id, t1.dtype, t0.target_id from source_base t0 left join target_base t1 on t1.id = t0.target_id order by t0.pos");
  }

  @Test
  public void selectSourceASql() {

    final Query<SourceA> query = DB.find(SourceA.class).orderBy("pos");
    query.findList();

    assertThat(sqlOf(query)).contains("select t0.dtype, t0.id, t0.name, t0.pos, t1.dtype, t0.target_id from source_base t0 left join target_base t1 on t1.id = t0.target_id where t0.dtype = 'SourceA' order by t0.pos");
  }

  @Test
  public void selectSourceAWithJoin() {

    final Query<SourceA> query = DB.find(SourceA.class).fetch("target", "name").orderBy("pos");
    query.findList();

    assertThat(sqlOf(query)).contains("select t0.dtype, t0.id, t0.name, t0.pos, t1.dtype, t1.id, t1.name from source_base t0 left join target_base t1 on t1.id = t0.target_id and t1.dtype = 'Target1' where t0.dtype = 'SourceA' order by t0.pos");
  }

  @Test
  public void test() {

    Target1 target1 = new Target1("target 1");
    Target2 target2 = new Target2("target 2");
    Target1 target1b = new Target1("target 1b");

    SourceA sourceA = new SourceA("source a", target1, 1);
    SourceB sourceB = new SourceB("source b", target2, 2);
    SourceA sourceA2 = new SourceA("source a2", target1b, 3);

    DB.saveAll(asList(sourceA, sourceB, sourceA2));

    final Database db = DB.getDefault();

    final SourceBase foundA = DB.find(SourceBase.class, db.beanId(sourceA));
    final SourceBase foundB = DB.find(SourceBase.class, db.beanId(sourceB));

    assertSourceBaseEqual(foundA, sourceA);
    assertThat(foundA).isInstanceOf(SourceA.class);
    assertSourceAEqual((SourceA) foundA, sourceA);

    assertSourceBaseEqual(foundB, sourceB);
    assertThat(foundB).isInstanceOf(SourceB.class);
    assertSourceBEqual((SourceB) foundB, sourceB);

    assertFetchAllDoubleLazyLoading();
    assertFetchAllSourceAs();
  }

  private void assertFetchAllSourceAs() {

    LoggedSql.start();

    final List<SourceA> sourceAList = DB.find(SourceA.class)
      .fetch("target", "name")
      .orderBy("pos")
      .findList();

    final String joinedNames = sourceAList.stream()
      .map(it -> "|" + it.getName() + "|" + it.getTarget().getName())
      .collect(Collectors.joining());

    assertThat(joinedNames).isEqualTo("|source a|target 1|source a2|target 1b");

    final List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("select t0.dtype, t0.id, t0.name, t0.pos, t1.dtype, t1.id, t1.name from source_base t0 left join target_base t1 on t1.id = t0.target_id and t1.dtype = 'Target1' where t0.dtype = 'SourceA' order by t0.pos");
  }

  /**
   * Expect separate lazy loading for SourceAs and SourceBs.
   */
  private void assertFetchAllDoubleLazyLoading() {

    LoggedSql.start();

    final List<SourceBase> sources = DB.find(SourceBase.class).orderBy("pos").findList();
    for (SourceBase source : sources) {
      if (source instanceof SourceA) {
        SourceA a = (SourceA) source;
        final Target1 target = a.getTarget();
        System.out.println("read target 1 " + target.getId() + " " + target.getName());

      } else if (source instanceof SourceB) {
        SourceB b = (SourceB) source;
        final Target2 target = b.getTarget();
        System.out.println("read target 2 " + target.getId() + " " + target.getName());
      }
    }

    final List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(3);
    //assertSql(sql.get(0)).contains("select t0.dtype, t0.id, t0.name, t0.pos, t0.target_id, t0.target_id from source_base t0 order by t0.pos");
    assertSql(sql.get(0)).contains("select t0.dtype, t0.id, t0.name, t0.pos, t1.dtype, t0.target_id, t1.dtype, t0.target_id from source_base t0 left join target_base t1 on t1.id = t0.target_id order by t0.pos");
    assertSql(sql.get(1)).contains("select t0.dtype, t0.id, t0.name from target_base t0 where t0.dtype = 'Target1' and t0.id ");
    assertSql(sql.get(2)).contains("select t0.dtype, t0.id, t0.name from target_base t0 where t0.dtype = 'Target2' and t0.id = ?");
  }

  private void assertSourceBaseEqual(SourceBase foundA, SourceBase sourceA) {
    assertThat(foundA.getId()).isEqualTo(sourceA.getId());
    assertThat(foundA.getName()).isEqualTo(sourceA.getName());
  }

  private void assertSourceAEqual(SourceA found, SourceA source) {
    final Target1 target = found.getTarget();
    final String name = target.getName();
    assertThat(name).isEqualTo(source.getTarget().getName());

    assertThat(found.getTarget().getId()).isEqualTo(source.getTarget().getId());
    assertThat(found.getTarget().getName()).isEqualTo(source.getTarget().getName());
    assertThat(found.getTarget().getClass()).isEqualTo(source.getTarget().getClass());
  }

  private void assertSourceBEqual(SourceB found, SourceB source) {

    final Target2 target = found.getTarget();
    final String name = target.getName();
    assertThat(name).isEqualTo(source.getTarget().getName());

    assertThat(found.getTarget().getId()).isEqualTo(source.getTarget().getId());
    assertThat(found.getTarget().getName()).isEqualTo(source.getTarget().getName());
    assertThat(found.getTarget().getClass()).isEqualTo(source.getTarget().getClass());
  }
}
