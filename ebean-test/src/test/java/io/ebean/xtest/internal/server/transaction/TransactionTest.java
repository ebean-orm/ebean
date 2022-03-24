package io.ebean.xtest.internal.server.transaction;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.relates.Relation1;
import org.tests.model.basic.relates.Relation2;
import org.tests.model.basic.relates.Relation3;
import org.tests.model.basic.relates.Relation4;

class TransactionTest extends BaseTestCase {

  private final Relation1 r1 = new Relation1("R1");
  private final Relation2 r2 = new Relation2("R2");
  private final Relation3 r3 = new Relation3("R3");

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
  void testMultiSave1() {
    r2.setWithCascade(r3);
    r1.setWithCascade(r2);
    DB.save(r1);
  }

  @Test
  void testMultiSave2() {
    r2.setWithCascade(r3);
    r1.setWithCascade(r2);
    DB.save(r3);
    DB.save(r1);
  }

  @Test
  void testMultiSave3() {
    r2.setWithCascade(r3);
    r1.setWithCascade(r2);
    DB.save(r3);
    DB.save(r2);
    DB.save(r1);
  }

  @Test
  void testMultiSave4() {
    r2.setNoCascade(r3);
    r1.setWithCascade(r2);
    DB.save(r3);
    // Workaround: txn.flush();
    DB.save(r1);
  }

  @Test
  void testMultiSave4_usingRelation4() {
    Relation4 r4 = new Relation4("foo");
    r2.setR4NoCascade(r4);
    r1.setWithCascade(r2);
    DB.save(r4);
    DB.save(r1);
  }

  @Test
  void testMultiSave5() {
    r2.setNoCascade(r3);
    r1.setNoCascade(r2);
    DB.save(r3);
    DB.save(r2);
    DB.save(r1);
  }
}
