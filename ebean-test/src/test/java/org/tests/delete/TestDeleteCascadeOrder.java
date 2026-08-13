package org.tests.delete;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.tests.model.deleteorder.DcoAsset;
import org.tests.model.deleteorder.DcoLinkAdapter;
import org.tests.model.deleteorder.DcoParent;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A join entity owns the foreign key to the bean its delete cascades to, so the join row has to be
 * deleted first. When a persist callback writes to the database it flushes the batch from inside the
 * flush that is already running : the outer flush has taken the join rows out of their bean holder,
 * so the inner flush only finds the assets and executes them first.
 * <p>
 * See <a href="https://github.com/ebean-orm/ebean/issues/1852">#1852</a>.
 */
class TestDeleteCascadeOrder extends BaseTestCase {

  @AfterEach
  void after() {
    DcoLinkAdapter.reset();
  }

  @Test
  void deleteLinkBeforeAsset() {
    assertLinkDeletedBeforeAsset(deleteAllLinks(newParent(2)));
  }

  @Test
  void deleteLinkBeforeAsset_whenCallbackWritesOnPreDelete() {
    DcoLinkAdapter.writeOnPreDelete(true);

    assertLinkDeletedBeforeAsset(deleteAllLinks(newParent(2)));
  }

  @Test
  void deleteLinkBeforeAsset_whenCallbackWritesOnPostDelete() {
    DcoLinkAdapter.writeOnPostDelete(true);

    assertLinkDeletedBeforeAsset(deleteAllLinks(newParent(2)));
  }

  private Long newParent(int assetCount) {
    DcoParent parent = new DcoParent("parent-" + assetCount);
    for (int i = 0; i < assetCount; i++) {
      parent.addAsset(new DcoAsset("asset-" + i));
    }
    DB.save(parent);
    return parent.getId();
  }

  /**
   * Remove every link of the parent, which cascades the delete to the assets behind them. The graph is
   * fetched up front : a lazy load would flush the batch on its own and hide the ordering.
   */
  private List<String> deleteAllLinks(Long parentId) {
    try (Transaction txn = DB.beginTransaction()) {
      txn.setBatchMode(true);
      DcoParent parent = DB.find(DcoParent.class)
        .fetch("links")
        .fetch("links.asset")
        .where().idEq(parentId)
        .findOne();
      parent.getLinks().clear();

      LoggedSql.start();
      DB.save(parent);
      txn.commit();
      return LoggedSql.stop();
    }
  }

  private void assertLinkDeletedBeforeAsset(List<String> sql) {
    assertThat(firstIndexOf(sql, "delete from dco_link"))
      .as("the join row must be deleted before the asset it references, statements were :%n%s", String.join("\n", sql))
      .isLessThan(firstIndexOf(sql, "delete from dco_asset"));
  }

  private int firstIndexOf(List<String> sql, String fragment) {
    for (int i = 0; i < sql.size(); i++) {
      if (sql.get(i).contains(fragment)) {
        return i;
      }
    }
    throw new AssertionError("no statement containing '" + fragment + "', statements were :\n" + String.join("\n", sql));
  }
}
