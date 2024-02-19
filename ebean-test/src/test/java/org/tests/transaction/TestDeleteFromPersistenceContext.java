package org.tests.transaction;

import io.ebean.Transaction;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebeaninternal.api.SpiTransaction;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;
import org.tests.model.basic.ResetBasicData;

import static org.junit.jupiter.api.Assertions.*;

public class TestDeleteFromPersistenceContext extends BaseTestCase {

  @Test
  public void testDeleteBean() {

    ResetBasicData.reset();

    EBasicVer bean = new EBasicVer("Please Delete Me");
    DB.save(bean);

    try (Transaction txn = DB.beginTransaction()) {
      SpiTransaction spiTransaction = (SpiTransaction)txn;
      EBasicVer bean2 = DB.find(EBasicVer.class, bean.getId());
      assertNotSame(bean, bean2);

      EBasicVer bean3 = DB.find(EBasicVer.class, bean.getId());
      // same instance from PersistenceContext
      assertSame(bean2, bean3);

      Object bean4 = spiTransaction.persistenceContext().get(EBasicVer.class, bean.getId());
      assertSame(bean2, bean4);

      DB.delete(bean2);

      Object bean5 = spiTransaction.persistenceContext().get(EBasicVer.class, bean.getId());
      assertNull(bean5);

      txn.commit();
    }

    EBasicVer bean6 = DB.find(EBasicVer.class).where().eq("id", bean.getId()).findOne();
    assertNull(bean6);

    awaitL2Cache();
    EBasicVer bean7 = DB.find(EBasicVer.class, bean.getId());
    assertNull(bean7);
  }

}
