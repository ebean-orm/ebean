package org.tests.model.basic.xtra;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import org.tests.model.basic.EBasicVer;
import org.junit.Assert;

import javax.persistence.PersistenceException;

public class OptimisticLockExceptionThrowingDao {

  Transaction inMethodTransaction;

  @Transactional
  public void doSomething(EBasicVer v) {

    EbeanServer server = Ebean.getServer(null);

    inMethodTransaction = server.currentTransaction();

    Transaction t = server.createTransaction();
    SqlUpdate u = server.createSqlUpdate("update e_basicver set last_update = last_update+1 where id = ?");
    u.setParameter(1, v.getId());
    int count = server.execute(u, t);
    Assert.assertEquals(1, count);

    t.commit();

    v.setName("some change");
    try {
      Ebean.save(v);
      // never get here
      Assert.assertTrue(false);
    } catch (PersistenceException e) {
      throw e;
    }

  }

  public Transaction getInMethodTransaction() {
    return inMethodTransaction;
  }

}
