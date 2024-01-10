package org.tests.persistencecontext;

import io.ebean.Transaction;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;

import static io.ebean.PersistenceContextScope.QUERY;
import static io.ebean.PersistenceContextScope.TRANSACTION;
import static org.junit.jupiter.api.Assertions.*;


class TestPersistenceContextQueryScope extends BaseTestCase {

  @Test
  void test() {

    EBasicVer bean = new EBasicVer("first");
    DB.save(bean);

    try (Transaction txn = DB.beginTransaction()) {
      EBasicVer bean1 = DB.find(EBasicVer.class, bean.getId());

      // do an update of the name in the DB
      int rowCount = DB.sqlUpdate("update e_basicver set name=? where id=?")
        .setParameter("second")
        .setParameter(bean.getId())
        .execute();

      assertEquals(1, rowCount);

      // fetch the bean again... but doesn't hit DB as it
      // is in the PersistenceContext which is transaction scoped
      EBasicVer bean2 = DB.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .findOne();

      // QUERY scope hits the DB (doesn't use the existing transactions persistence context)
      // ... also explicitly not use bean cache
      EBasicVer bean3 = DB.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .setPersistenceContextScope(QUERY)
        .findOne();

      // TRANSACTION scope ... same as bean2 and does not hit the DB
      EBasicVer bean5 = DB.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .setPersistenceContextScope(TRANSACTION)
        .findOne();

      assertEquals("first", bean.getName());
      assertEquals("first", bean1.getName());
      assertEquals("second", bean2.getName());
      assertEquals("second", bean3.getName());
      assertEquals("second", bean5.getName());
      assertNotSame(bean1, bean2);
      assertNotSame(bean1, bean5);
      assertSame(bean2, bean5);
      assertNotSame(bean3, bean5);

      DB.delete(bean3);

      txn.commit();
    }
  }

  @Test
  void ormUpdateQuery_expect_clearsContext() {

    EBasicVer bean = new EBasicVer("first");
    DB.save(bean);

    try (Transaction txn = DB.beginTransaction()) {
      EBasicVer bean1 = DB.find(EBasicVer.class, bean.getId());

      // do an update of the name in the DB
      int rowCount = DB.update(EBasicVer.class)
        .set("name", "second")
        .where().idEq(bean.getId())
        .update();
      assertEquals(1, rowCount);

      // fetch the bean again... but doesn't hit DB as it
      // is in the PersistenceContext which is transaction scoped
      EBasicVer bean2 = DB.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .findOne();

      // QUERY scope hits the DB (doesn't use the existing transactions persistence context)
      // ... also explicitly not use bean cache
      EBasicVer bean3 = DB.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .setPersistenceContextScope(QUERY)
        .findOne();

      // TRANSACTION scope ... same as bean2 and does not hit the DB
      EBasicVer bean5 = DB.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .setPersistenceContextScope(TRANSACTION)
        .findOne();

      assertEquals("first", bean.getName());
      assertEquals("first", bean1.getName());
      assertEquals("second", bean2.getName());
      assertEquals("second", bean3.getName());
      assertEquals("second", bean5.getName());
      assertNotSame(bean1, bean2);
      assertNotSame(bean1, bean5);
      assertSame(bean2, bean5);
      assertNotSame(bean3, bean5);

      DB.delete(bean3);
      txn.commit();
    }
  }


  @Test
  void ormUpdate_expect_clearsContext() {

    EBasicVer bean = new EBasicVer("first");
    DB.save(bean);

    try (Transaction txn = DB.beginTransaction()) {
      EBasicVer bean1 = DB.find(EBasicVer.class, bean.getId());

      // do an update of the name in the DB
      int rowCount = DB.createUpdate(EBasicVer.class, "update ebasicver set name = ? where id = ?")
        .setParameter(1, "second")
        .setParameter(2, bean.getId())
        .execute();
      assertEquals(1, rowCount);

      // fetch the bean again... but doesn't hit DB as it
      // is in the PersistenceContext which is transaction scoped
      EBasicVer bean2 = DB.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .findOne();

      // QUERY scope hits the DB (doesn't use the existing transactions persistence context)
      // ... also explicitly not use bean cache
      EBasicVer bean3 = DB.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .setPersistenceContextScope(QUERY)
        .findOne();

      // TRANSACTION scope ... same as bean2 and does not hit the DB
      EBasicVer bean5 = DB.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .setPersistenceContextScope(TRANSACTION)
        .findOne();

      assertEquals("first", bean.getName());
      assertEquals("first", bean1.getName());
      assertEquals("second", bean2.getName());
      assertEquals("second", bean3.getName());
      assertEquals("second", bean5.getName());
      assertNotSame(bean1, bean2);
      assertNotSame(bean1, bean5);
      assertSame(bean2, bean5);
      assertNotSame(bean3, bean5);

      DB.delete(bean3);
      txn.commit();
    }
  }


  @Test
  void ormUpdateQueryWithAutoPersist_expect_clearsContext() {

    EBasicVer bean = new EBasicVer("first");
    DB.save(bean);

    try (Transaction txn = DB.beginTransaction()) {
      txn.setAutoPersistUpdates(true);
      EBasicVer bean1 = DB.find(EBasicVer.class, bean.getId());

      // do an update of the name in the DB
      int rowCount = DB.update(EBasicVer.class)
        .set("name", "second")
        .where().idEq(bean.getId())
        .update();
      assertEquals(1, rowCount);

      // fetch the bean again... but doesn't hit DB as it
      // is in the PersistenceContext which is transaction scoped
      EBasicVer bean2 = DB.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .findOne();

      // QUERY scope hits the DB (doesn't use the existing transactions persistence context)
      // ... also explicitly not use bean cache
      EBasicVer bean3 = DB.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .setPersistenceContextScope(QUERY)
        .findOne();

      // TRANSACTION scope ... same as bean2 and does not hit the DB
      EBasicVer bean5 = DB.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .setPersistenceContextScope(TRANSACTION)
        .findOne();

      assertEquals("first", bean.getName());
      assertEquals("first", bean1.getName());
      assertEquals("first", bean2.getName()); // This is still first
      assertEquals("second", bean3.getName());
      assertEquals("first", bean5.getName()); // This is still first
      assertSame(bean1, bean2); // Now the same
      assertSame(bean1, bean5); // Now the same
      assertSame(bean2, bean5);
      assertNotSame(bean3, bean5);

      DB.delete(bean3);
      txn.commit();
    }
  }
}
