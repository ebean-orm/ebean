package org.tests.model.basic.xtra;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.SqlUpdate;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import org.tests.model.basic.EBasicVer;

import javax.persistence.PersistenceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class OptimisticLockExceptionThrowingDao {

  Transaction inMethodTransaction;

  @Transactional
  public void doSomething(EBasicVer v) {
    Database server = DB.getDefault();

    inMethodTransaction = server.currentTransaction();

    Transaction t = server.createTransaction();
    SqlUpdate u = server.sqlUpdate("update e_basicver set last_update = last_update+1 where id = ?");
    u.setParameter(1, v.getId());
    int count = server.execute(u, t);
    assertEquals(1, count);

    t.commit();

    v.setName("some change");
    try {
      DB.save(v);
      // never get here
      fail();
    } catch (PersistenceException e) {
      throw e;
    }

  }

  public Transaction getInMethodTransaction() {
    return inMethodTransaction;
  }

}
