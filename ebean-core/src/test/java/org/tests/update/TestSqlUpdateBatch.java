package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Testclass that ensures the correct behaviour of SqlUpdate in combination with batching.
 *
 * @author Jonas P&ouml;hler, FOCONIS AG
 */
public class TestSqlUpdateBatch extends BaseTestCase {

  /**
   * This test ensures the correct behaviour of update batching in the {@code BatchedPstmtHolder} after a flush. Two batch updates
   * are executed with each a batchsize of 21, which is the default limit for automatic flushing on {@code addBatch()}. After that
   * the statements are each executed with {@code executeBatch()}. The desired behaviour is, that each execution succeeds although
   * technically the batch was already flushed.
   */
  @Test
  public void testTwoParallelBatches() {
    try (Transaction txn = DB.beginTransaction()) {
      // Dummy updates, that effectively do nothing, but ebean doesn't need to know this.
      final SqlUpdate update = DB.sqlUpdate("update uuone set name = ? where 0=1");
      final SqlUpdate delete = DB.sqlUpdate("delete from uuone where ?=-1");

      for (int i = 0; i < 20; i++) {
        update
          .setParameter(1, String.valueOf(i))
          .addBatch();
        delete
          .setParameter(1, i)
          .addBatch();
      }

      assertThat(delete.executeBatch().length).isEqualTo(20);
      assertThat(update.executeBatch().length).isEqualTo(20);
      txn.commit();
    }
  }

  /**
   * This test checks that for a batch update a correct array with update counts is returned. If a update with 40 entries is
   * executed, it is expected, that {@code executeBatch()} returns a array with 40 elements each containing the update count for
   * the given parameters.
   */
  @Test
  public void testBatchReturnArrayLength() {
    try (Transaction txn = DB.beginTransaction()) {
      // transaction batch size is ignored with addBatch()
      txn.setBatchSize(10);
      // Dummy update, that effectively does nothing, but ebean doesn't need to know this.
      final SqlUpdate update = DB.sqlUpdate("update uuone set name = ? where 0=1");
      for (int i = 0; i < 40; i++) {
        update
          .setParameter(1, String.valueOf(i))
          .addBatch();
      }
      assertEquals(40, update.executeBatch().length);
    }
  }

}
