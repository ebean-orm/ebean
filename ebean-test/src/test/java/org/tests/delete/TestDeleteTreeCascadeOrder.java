package org.tests.delete;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.model.deleteorder.DcoTree;
import org.tests.model.deleteorder.DcoTreeContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The tree shape reported on <a href="https://github.com/ebean-orm/ebean/issues/1852">#1852</a> :
 * deleting the container cascades down a self referencing tree, and the deletes have to reach the
 * leaves before their parents.
 */
class TestDeleteTreeCascadeOrder extends BaseTestCase {

  /**
   * Still reproduces on 18.4.0. The tree is deleted level by level but not deepest first :
   * <pre>
   *   delete from dco_tree where id in (?)      -- the root, whose children are still there
   *   delete from dco_tree where id in (?,?,?)
   *   delete from dco_tree where id in (?,?)
   * </pre>
   * which fails with "Referential integrity constraint violation: FK_DCO_TREE_PARENT_ID". Disabled so
   * that it does not break the build, remove the annotation to see the failure.
   */
  @Disabled("reproduces #1852, not fixed yet")
  @Test
  void deleteContainerOfNestedTree() {
    DcoTreeContainer container = new DcoTreeContainer();
    DcoTree root = new DcoTree("root");

    DcoTree child1 = root.addChild("child 1");
    child1.addChild("child 1a").addChild("child 1a1");

    DcoTree child2 = root.addChild("child 2");
    child2.addChild("child 2a");
    child2.addChild("child 2b");

    container.getTrees().add(root);
    DB.save(container);

    DB.delete(container);

    assertThat(DB.find(DcoTree.class).findCount()).isZero();
    assertThat(DB.find(DcoTreeContainer.class).where().idEq(container.getId()).findCount()).isZero();
  }
}
