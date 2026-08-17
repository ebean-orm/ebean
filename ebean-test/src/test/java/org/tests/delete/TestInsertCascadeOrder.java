package org.tests.delete;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.tests.model.deleteorder.DcoAsset;
import org.tests.model.deleteorder.DcoParent;
import org.tests.model.deleteorder.DcoParentAdapter;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Same defect as the cascaded delete one, on the insert side : a persist done from a BeanPersistController
 * flushes the batch from inside the flush that is already running, and the statements queued behind the
 * one being executed are issued out of order.
 * <p>
 * <a href="https://github.com/ebean-orm/ebean/pull/3148">#3148</a> fixed this for a flush triggered by a
 * query (BatchControl.executeNow disables flushOnQuery), but a flush triggered by a write goes through
 * BatchControl.executeOrQueue, which that guard does not cover.
 */
class TestInsertCascadeOrder extends BaseTestCase {

  @AfterEach
  void after() {
    DcoParentAdapter.writeOnPreInsert(false);
    DcoParentAdapter.sqlUpdateOnPreInsert(false);
  }

  @Test
  void insertParentBeforeItsLinks_whenCallbackWritesDuringFlush() {
    DcoParentAdapter.writeOnPreInsert(true);

    List<String> sql = insertParents(3);

    // every parent has to be inserted before the link that points at it
    assertThat(lastIndexOf(sql, "insert into dco_parent"))
      .as("a parent must be inserted before the links referencing it, statements were :%n%s", String.join("\n", sql))
      .isLessThan(lastIndexOf(sql, "insert into dco_link"));
  }

  /**
   * Same as above but the callback runs a SqlUpdate, which reaches BatchControl by
   * executeStatementOrBatch rather than executeOrQueue.
   */
  @Test
  void insertParentBeforeItsLinks_whenCallbackRunsSqlUpdateDuringFlush() {
    DcoParentAdapter.sqlUpdateOnPreInsert(true);

    List<String> sql = insertParents(3);

    assertThat(lastIndexOf(sql, "insert into dco_parent"))
      .as("a parent must be inserted before the links referencing it, statements were :%n%s", String.join("\n", sql))
      .isLessThan(lastIndexOf(sql, "insert into dco_link"));
  }

  private List<String> insertParents(int count) {
    List<DcoParent> parents = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      DcoParent parent = new DcoParent("batch-parent-" + i);
      parent.addAsset(new DcoAsset("batch-asset-" + i));
      parents.add(parent);
    }

    try (Transaction txn = DB.beginTransaction()) {
      txn.setBatchMode(true);
      txn.setBatchSize(50);
      LoggedSql.start();
      DB.saveAll(parents);
      txn.commit();
      List<String> sql = LoggedSql.stop();
      System.out.println("---- insert order ----");
      sql.stream().filter(s -> !s.contains("-- bind")).forEach(s -> System.out.println("   " + s));
      return sql;
    }
  }

  private int lastIndexOf(List<String> sql, String fragment) {
    for (int i = sql.size() - 1; i >= 0; i--) {
      if (sql.get(i).contains(fragment)) {
        return i;
      }
    }
    throw new AssertionError("no statement containing '" + fragment + "', statements were :\n" + String.join("\n", sql));
  }
}
