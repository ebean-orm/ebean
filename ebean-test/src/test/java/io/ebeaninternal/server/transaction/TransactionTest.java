package io.ebeaninternal.server.transaction;

import io.ebean.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.relates.*;

public class TransactionTest extends BaseTestCase {
  private Relation1 r1 = new Relation1("R1");
  private Relation2 r2 = new Relation2("R2");
  private Relation3 r3 = new Relation3("R3");

  private Transaction txn;

  @BeforeEach
  void beginTransaction() {
    txn = DB.beginTransaction();
    txn.setBatchMode(true);
  }

  @AfterEach
  void commitTransaction() {
    txn.commit();
    txn.close();
  }

  @Test
  public void testMultiSave1() {
    r2.setWithCascade(r3);
    r1.setWithCascade(r2);
    DB.save(r1);
  }

  @Test
  public void testMultiSave2() {
    r2.setWithCascade(r3);
    r1.setWithCascade(r2);
    DB.save(r3);
    DB.save(r1);
  }

  @Test
  public void testMultiSave3() {
    r2.setWithCascade(r3);
    r1.setWithCascade(r2);
    DB.save(r3);
    DB.save(r2);
    DB.save(r1);
  }

  @Test
  public void testMultiSave4() {
    r2.setNoCascade(r3);
    r1.setWithCascade(r2);
    DB.save(r3);
    // Workaround: txn.flush();
    DB.save(r1);
  }

  @Test
  public void testMultiSave5() {
    r2.setNoCascade(r3);
    r1.setNoCascade(r2);
    DB.save(r3);
    DB.save(r2);
    DB.save(r1);
  }
}