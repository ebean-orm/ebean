package org.tests.persistencecontext;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import org.junit.Test;
import org.tests.model.basic.EBasicVer;

import static io.ebean.PersistenceContextScope.QUERY;
import static io.ebean.PersistenceContextScope.TRANSACTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;


public class TestPersistenceContextQueryScope extends BaseTestCase {

  @Test
  public void test() {

    EBasicVer bean = new EBasicVer("first");
    Ebean.save(bean);

    //Ebean.getServerCacheManager().setCaching(EBasicVer.class, true);

    Ebean.beginTransaction();
    try {
      EBasicVer bean1 = Ebean.find(EBasicVer.class, bean.getId());

      // do an update of the name in the DB
      SqlUpdate sqlUpdate = Ebean.createSqlUpdate("update e_basicver set name=? where id=?");
      sqlUpdate.setNextParameter("second");
      sqlUpdate.setNextParameter(bean.getId());
      int rowCount = sqlUpdate.execute();
      assertEquals(1, rowCount);

      // fetch the bean again... but doesn't hit DB as it
      // is in the PersistenceContext which is transaction scoped
      EBasicVer bean2 = Ebean.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .findOne();

      // QUERY scope hits the DB (doesn't use the existing transactions persistence context)
      // ... also explicitly not use bean cache
      EBasicVer bean3 = Ebean.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .setPersistenceContextScope(QUERY)
        .findOne();

      // TRANsACTION scope ... same as bean2 and does not hit the DB
      EBasicVer bean5 = Ebean.find(EBasicVer.class)
        .setId(bean.getId())
        .setUseCache(false) // ignore L2 cache
        .setPersistenceContextScope(TRANSACTION)
        .findOne();

      assertEquals("first", bean.getName());
      assertEquals("first", bean1.getName());
      assertEquals("first", bean2.getName());
      assertEquals("first", bean5.getName());
      assertSame(bean1, bean2);
      assertSame(bean1, bean5);

      assertEquals("second", bean3.getName());
      Ebean.delete(bean3);

      Ebean.commitTransaction();

    } finally {
      Ebean.endTransaction();
    }
  }
}
