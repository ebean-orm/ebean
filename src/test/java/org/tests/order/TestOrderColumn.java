package org.tests.order;

import io.ebean.Ebean;
import io.ebean.TransactionalTestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOrderColumn extends TransactionalTestCase {

  @Test
  public void testOrderColumnInheritance() {
    final OrderMaster master = new OrderMaster();

    for (int i = 0; i < 5; i++) {
      final OrderReferencedChild child = new OrderReferencedChild("p" + i);
      child.setChildName("c" + i);

      master.getChildren().add(child);
    }

    Ebean.save(master);

    final OrderMaster result = Ebean.find(OrderMaster.class).findOne();

    assertThat(result.getChildren()).hasSize(5);
    assertThat(result.getChildren()).extracting(OrderReferencedChild::getName).containsExactly("p0", "p1", "p2", "p3", "p4");
    assertThat(result.getChildren()).extracting(OrderReferencedChild::getChildName).containsExactly("c0", "c1", "c2", "c3", "c4");
  }

}
