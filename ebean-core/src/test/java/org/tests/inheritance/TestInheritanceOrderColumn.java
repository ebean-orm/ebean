package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebeantest.LoggedSql;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.inheritance.order.OrderMasterInheritance;
import org.tests.inheritance.order.OrderedA;
import org.tests.inheritance.order.OrderedB;
import org.tests.inheritance.order.OrderedParent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInheritanceOrderColumn extends BaseTestCase {

  @Test
  public void test() {
    final OrderMasterInheritance master = new OrderMasterInheritance();
    final OrderedA orderedA = new OrderedA();
    orderedA.setCommonName("commonOrderedA");
    orderedA.setOrderedAName("orderedA");

    final OrderedB orderedB = new OrderedB();
    orderedB.setCommonName("commonOrderedB");
    orderedB.setOrderedBName("orderedB");

    master.getReferenced().add(orderedA);
    master.getReferenced().add(orderedB);

    LoggedSqlCollector.start();
    Ebean.save(master);
    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(5);
    // Some platforms insert ids and others don't need to...
    assertSql(trimId(sql, 1))
      .contains("insert into ordered_parent (order_master_inheritance_id, dtype, common_name, ordered_aname, sort_order) values");
    assertSql(trimId(sql, 3))
      .contains("insert into ordered_parent (order_master_inheritance_id, dtype, common_name, ordered_bname, sort_order) values");

    OrderMasterInheritance result = Ebean.find(OrderMasterInheritance.class).findOne();
    assertThat(result.getReferenced())
      .extracting(OrderedParent::getCommonName)
      .containsExactly("commonOrderedA", "commonOrderedB");

    // Swap the two
    result.getReferenced().add(0, result.getReferenced().remove(1));
    assertThat(result.getReferenced())
      .extracting(OrderedParent::getCommonName)
      .containsExactly("commonOrderedB", "commonOrderedA");

    LoggedSql.start();
    Ebean.save(result);
    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(3);
    assertThat(sql.get(0)).contains("update ordered_parent set sort_order=? where id=?");

    result = Ebean.find(OrderMasterInheritance.class).findOne();
    assertThat(result.getReferenced())
      .extracting(OrderedParent::getCommonName)
      .containsExactly("commonOrderedB", "commonOrderedA");
  }

  private String trimId(List<String> sql, int i) {
    return sql.get(i).replace("(id, ", "(");
  }

}
