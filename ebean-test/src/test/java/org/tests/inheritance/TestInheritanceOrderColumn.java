package org.tests.inheritance;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.inheritance.order.OrderMasterInheritance;
import org.tests.inheritance.order.OrderedParent;
import org.tests.inheritance.order.OrderedParent;
import org.tests.inheritance.order.OrderedParent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInheritanceOrderColumn extends BaseTestCase {

  @Test
  public void test() {
    final OrderMasterInheritance master = new OrderMasterInheritance();
    final OrderedParent orderedA = new OrderedParent();
    orderedA.setCommonName("commonOrderedParent");
    orderedA.setOrderedParentName("orderedA");

    final OrderedParent orderedB = new OrderedParent();
    orderedB.setCommonName("commonOrderedParent");
    orderedB.setOrderedParentName("orderedB");

    master.getReferenced().add(orderedA);
    master.getReferenced().add(orderedB);

    LoggedSql.start();
    DB.save(master);
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(5);
    // Some platforms insert ids and others don't need to...
    assertSql(trimId(sql, 1))
      .contains("insert into ordered_parent (order_master_inheritance_id, common_name, ordered_aname, ordered_bname, sort_order) values");
    assertSql(sql.get(2)).contains("-- bind");
    assertSql(sql.get(3)).contains("-- bind");

    OrderMasterInheritance result = DB.find(OrderMasterInheritance.class).findOne();
    assertThat(result.getReferenced())
      .extracting(OrderedParent::getCommonName)
      .containsOnly("orderedA", "orderedB");

    // Swap the two
    result.getReferenced().add(0, result.getReferenced().remove(1));
    assertThat(result.getReferenced())
      .extracting(OrderedParent::getCommonName)
      .containsOnly("orderedA", "orderedB");

    LoggedSql.start();
    DB.save(result);
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(4);
    assertThat(sql.get(0)).contains("update ordered_parent set sort_order=? where id=?");

    result = DB.find(OrderMasterInheritance.class).findOne();
    assertThat(result.getReferenced())
      .extracting(OrderedParent::getCommonName)
      .containsOnly("orderedA", "orderedB");
  }

  private String trimId(List<String> sql, int i) {
    return sql.get(i).replace("(id, ", "(");
  }

}
