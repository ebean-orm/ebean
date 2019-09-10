package io.ebean;

import io.ebean.annotation.PersistBatch;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TxScopeTest {

  @Test
  public void checkBatchMode_when_bothNull() throws Exception {

    TxScope scope = new TxScope();
    scope.setBatchSize(100);

    assertNull(scope.getBatch());
    assertNull(scope.getBatchOnCascade());

    scope.checkBatchMode();
    assertEquals(scope.getBatch(), PersistBatch.ALL);
  }

  @Test
  public void checkBatchMode_when_bothInherit() throws Exception {

    TxScope scope = new TxScope();
    scope.setBatchSize(100);
    scope.setBatch(PersistBatch.INHERIT);
    scope.setBatchOnCascade(PersistBatch.INHERIT);

    scope.checkBatchMode();
    assertEquals(scope.getBatch(), PersistBatch.ALL);
  }

  @Test
  public void checkBatchMode_when_batchSizeZero_and_onCascadeInherit() throws Exception {

    TxScope scope = new TxScope();
    scope.setBatchOnCascade(PersistBatch.INHERIT);

    scope.checkBatchMode();
    assertNull(scope.getBatch());
  }

  @Test
  public void checkBatchMode_when_batchSizeZero_and_bothInherit() throws Exception {

    TxScope scope = new TxScope();
    scope.setBatch(PersistBatch.INHERIT);
    scope.setBatchOnCascade(PersistBatch.INHERIT);

    scope.checkBatchMode();
    assertEquals(scope.getBatch(), PersistBatch.INHERIT);
  }

  @Test
  public void checkBatchMode_when_onCascadeSet() throws Exception {

    TxScope scope = new TxScope();
    scope.setBatchSize(100);
    scope.setBatchOnCascade(PersistBatch.ALL);

    scope.checkBatchMode();
    assertNull(scope.getBatch());
  }

}
