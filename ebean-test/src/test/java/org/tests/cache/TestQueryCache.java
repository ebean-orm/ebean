package org.tests.cache;

import io.ebean.CacheMode;
import io.ebean.DB;
import io.ebean.ExpressionList;
import io.ebean.Query;
import io.ebean.annotation.Transactional;
import io.ebean.annotation.TxIsolation;
import io.ebean.bean.BeanCollection;
import io.ebean.cache.ServerCache;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.cache.EColAB;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

public class TestQueryCache extends BaseTestCase {

  @Test
  public void clashHashCode() {

    new EColAB("01", "20").save();
    new EColAB("02", "10").save();

    List<EColAB> list1 =
      DB.find(EColAB.class)
        .setUseQueryCache(true)
        .where()
        .eq("columnA", "01")
        .eq("columnB", "20")
        .findList();

    List<EColAB> list2 =
      DB.find(EColAB.class)
        .setUseQueryCache(true)
        .where()
        .eq("columnA", "02")
        .eq("columnB", "10")
        .findList();

    assertThat(list1.get(0).getColumnA()).isEqualTo("01");
    assertThat(list1.get(0).getColumnB()).isEqualTo("20");

    assertThat(list2.get(0).getColumnA()).isEqualTo("02");
    assertThat(list2.get(0).getColumnB()).isEqualTo("10");
  }

  @Test
  public void findSingleAttributeList() {

    DB.find(EColAB.class).delete();
    new EColAB("03", "SingleAttribute").save();
    new EColAB("03", "SingleAttribute").save();

    List<String> colA_first = DB
      .find(EColAB.class)
      .setUseQueryCache(true)
      .setDistinct(true)
      .select("columnA")
      .where()
      .eq("columnB", "SingleAttribute")
      .findSingleAttributeList();

    List<String> colA_Second = DB
      .find(EColAB.class)
      .setUseQueryCache(true)
      .setDistinct(true)
      .select("columnA")
      .where()
      .eq("columnB", "SingleAttribute")
      .findSingleAttributeList();

    assertThat(colA_Second).isSameAs(colA_first);

    List<String> colA_NotDistinct = DB
      .find(EColAB.class)
      .setUseQueryCache(true)
      .select("columnA")
      .where()
      .eq("columnB", "SingleAttribute")
      .findSingleAttributeList();

    assertThat(colA_Second).isNotSameAs(colA_NotDistinct);

    // ensure that findCount & findSingleAttribute use different
    // slots in cache. If not a "Cannot cast List to int" should happen.
    int count = DB
      .find(EColAB.class)
      .setUseQueryCache(true)
      .select("columnA")
      .where()
      .eq("columnB", "SingleAttribute")
      .findCount();
    assertThat(count).isEqualTo(2);
  }

  @Test
  public void findSingleAttributeSet() {

    DB.find(EColAB.class).delete();
    new EColAB("03", "SingleAttribute").save();
    new EColAB("03", "SingleAttribute").save();
    ExpressionList<EColAB> query = DB
      .find(EColAB.class)
      .setUseQueryCache(true)
      .select("columnA")
      .where()
      .eq("columnB", "SingleAttribute");
    Set<String> colA_first = query.findSingleAttributeSet();

    Set<String> colA_Second = query.findSingleAttributeSet();

    assertThat(colA_Second).isSameAs(colA_first).hasSize(1);
    assertThatThrownBy(colA_first::clear).isInstanceOf(UnsupportedOperationException.class);
    // ensure, that we do not have cache collisions on same query
    query.findSingleAttributeList();
    query.findSingleAttribute();
    query.findCount();
  }

  @Test
  public void findCount() {

    new EColAB("04", "count").save();
    new EColAB("05", "count").save();

    LoggedSql.start();

    int count0 = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.ON)
      .where()
      .eq("columnB", "count")
      .findCount();

    int count1 = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.ON)
      .where()
      .eq("columnB", "count")
      .findCount();

    List<String> sql = LoggedSql.stop();

    assertThat(count0).isEqualTo(count1);
    assertThat(sql).hasSize(1);

    // and now, ensure that we hit the database
    LoggedSql.start();
    int count2 = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.OFF)
      .where()
      .eq("columnB", "count")
      .findCount();
    assertThat(count2).isEqualTo(count1);
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);

    LoggedSql.start();
    findCountNoTxn();
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(0);

    // Oracle does not support READ_UNCOMMITTED as expected
    if (!isOracle()) {
      LoggedSql.start();
      findCountTxn();
      sql = LoggedSql.stop();
      assertThat(sql).hasSize(0);
    }
  }

  private void findCountNoTxn() {
    DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.ON)
      .where()
      .eq("columnB", "count")
      .findCount();
  }

  @Transactional(isolation = TxIsolation.READ_UNCOMMITTED)
  private void findCountTxn() {
    findCountNoTxn();
  }

  @Test
  public void exists() {

    new EColAB("06", "exists").save();
    new EColAB("07", "exists").save();

    LoggedSql.start();

    boolean exists0 = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.ON)
      .where()
      .eq("columnB", "exists")
      .exists();

    boolean exists1 = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.ON)
      .where()
      .eq("columnB", "exists")
      .exists();

    List<String> sql = LoggedSql.stop();

    assertThat(exists0).isEqualTo(exists1);
    assertThat(sql).hasSize(1);

    // and now, ensure that we hit the database
    LoggedSql.start();
    boolean exists2 = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.OFF)
      .where()
      .eq("columnB", "exists")
      .exists();
    assertThat(exists2).isEqualTo(exists1);
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
  }

  @Test
  public void findCountDifferentQueries() {


    LoggedSql.start();

    int count0 = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.ON)
      .where()
      .eq("columnB", "abc")
      .findCount();

    int count1 = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.ON)
      .where()
      .eq("columnB", "def")
      .findCount();

    List<String> sql = LoggedSql.stop();

    assertThat(count0).isEqualTo(count1);
    assertThat(sql).hasSize(2); // different queries

  }

  @Test
  public void findCountFirstOnThenRecache() {


    LoggedSql.start();

    int count0 = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.ON)
      .where()
      .eq("columnB", "uvw")
      .findCount();

    int count1 = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.PUT)
      .where()
      .eq("columnB", "uvw")
      .findCount();

    List<String> sql = LoggedSql.stop();

    assertThat(count0).isEqualTo(count1);
    assertThat(sql).hasSize(2); // try recache as second query - it must fetch it

  }


  @Test
  public void findCountFirstRecacheThenOn() {


    LoggedSql.start();

    int count0 = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.PUT)
      .where()
      .eq("columnB", "xyz")
      .findCount();

    int count1 = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.ON)
      .where()
      .eq("columnB", "xyz")
      .findCount();

    List<String> sql = LoggedSql.stop();

    assertThat(count0).isEqualTo(count1);
    assertThat(sql).hasSize(1); // try recache as first query - second "ON" query must fetch it.

  }

  /**
   * This test primarily checks, if the query cache on findById will work properly.
   *
   * It also checks some special cases, if query + bean cache are combined with other queries.
   * It is important to know, that a "findId" query, that is not yet compiled will use the bean cache.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testFindByIdWihtBothCaches() {

    ResetBasicData.reset();

    List<Object> ids = DB.find(Customer.class).setMaxRows(5).findIds();
    Customer c = DB.find(Customer.class).setMaxRows(1).findOne();

    DB.getDefault().pluginApi().cacheManager().clearAll();

    ServerCache bc = DB.getDefault().pluginApi().cacheManager().beanCache(Customer.class);
    ServerCache qc = DB.getDefault().pluginApi().cacheManager().queryCache(Customer.class);
    bc.statistics(true);
    qc.statistics(true);

    // 1. load the bean cache with some beans
    DB.find(Customer.class).where().idIn(ids).findList();

    Query<Customer> q = DB.find(Customer.class).setUseQueryCache(true).setUseCache(true).setReadOnly(true);
    LoggedSql.start();
    Customer c1 = q.copy().where().eq("name", c.getName()).findOne();
    Customer c2 = q.copy().where().eq("name", c.getName()).findOne();
    assertThat(LoggedSql.stop()).hasSize(1);

    assertTrue(DB.beanState(c1).isReadOnly());
    assertTrue(DB.beanState(c2).isReadOnly());
    assertThat(c1).isSameAs(c2);
    assertThat(bc.statistics(true).getHitCount()).isEqualTo(0);
    assertThat(qc.statistics(true).getHitCount()).isEqualTo(1);


    LoggedSql.start();
    c1 = q.copy().where().eq("id", c.getId()).findOne();
    c2 = q.copy().where().eq("id", c.getId()).findOne();
    assertThat(LoggedSql.stop()).isEmpty();
    assertThat(c1).isNotSameAs(c2);

    LoggedSql.start();
    c1 = q.copy().setId(c.getId()).findOne();
    c2 = q.copy().setId(c.getId()).findOne();
    assertThat(LoggedSql.stop()).isEmpty();
    assertThat(c1).isNotSameAs(c2);

    LoggedSql.start();
    List<Customer> l1 = q.copy().where().idIn(ids.subList(0,2)).findList();
    List<Customer> l2 = q.copy().where().idIn(ids.subList(0,2)).findList();
    assertThat(LoggedSql.stop()).isEmpty();
    assertThat(l1).hasSize(2).isNotSameAs(l2);

    assertThat(bc.statistics(true).getHitCount()).isEqualTo(8); // 4x findOne and 2x findList with 2 elements
    assertThat(qc.statistics(true).getHitCount()).isEqualTo(0);
    // Note: The ID queries are immediately handled by the BeanCache, because the underlying queries are never compiled
    // and so they have no query plan, which is required for cache access.
    //
    // So we clear the cache and try it again
    DB.getDefault().pluginApi().cacheManager().clearAll();

    LoggedSql.start();
    c1 = q.copy().where().eq("id", c.getId()).findOne();
    c2 = q.copy().where().eq("id", c.getId()).findOne();
    assertThat(LoggedSql.stop()).hasSize(1);
    assertThat(c1).isSameAs(c2);

    LoggedSql.start();
    c1 = q.copy().setId(c.getId()).findOne();
    c2 = q.copy().setId(c.getId()).findOne();
    assertThat(LoggedSql.stop()).isEmpty(); // setId(..) query has same queryPlan as eq("id", ..)
    assertThat(c1).isSameAs(c2);

    LoggedSql.start();
    l1 = q.copy().where().idIn(1, 2).findList();
    l2 = q.copy().where().idIn(1, 2).findList();
    assertThat(LoggedSql.stop()).hasSize(1); // we have to hit DB for bean#2
    assertThat(l1).hasSize(2).isSameAs(l2);

    assertThat(bc.statistics(true).getHitCount()).isEqualTo(1); // findList can get one bean from bc
    assertThat(qc.statistics(true).getHitCount()).isEqualTo(4); // 6 queries, 2 of them hit db
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testReadOnlyFind() {

    ResetBasicData.reset();

    ServerCache customerCache = DB.cacheManager().queryCache(Customer.class);
    customerCache.clear();

    List<Customer> list = DB.find(Customer.class).setUseQueryCache(true).setReadOnly(true).where()
      .ilike("name", "Rob").findList();

    BeanCollection<Customer> bc = (BeanCollection<Customer>) list;
    assertTrue(bc.isReadOnly());
    assertFalse(bc.isEmpty());
    assertTrue(!list.isEmpty());
    assertTrue(DB.beanState(list.get(0)).isReadOnly());

    List<Customer> list2 = DB.find(Customer.class).setUseQueryCache(true).setReadOnly(true).where()
      .ilike("name", "Rob").findList();

    List<Customer> list2B = DB.find(Customer.class).setUseQueryCache(true)
      // .setReadOnly(true)
      .where().ilike("name", "Rob").findList();

    assertSame(list, list2);

    // readOnly defaults to true for query cache
    assertSame(list, list2B);

    List<Customer> list3 = DB.find(Customer.class).setUseQueryCache(true).setReadOnly(false).where()
      .ilike("name", "Rob").findList();

    assertNotSame(list, list3);
    BeanCollection<Customer> bc3 = (BeanCollection<Customer>) list3;
    assertFalse(bc3.isReadOnly());
    assertFalse(bc3.isEmpty());
    assertTrue(list3.size() > 0);
    // TODO: At this stage setReadOnly(false) does create a shallow copy of the List/Set/Map, but does not
    // change the read only state in the entities.
    // assertFalse(DB.beanState(list3.get(0)).isReadOnly());

  }

  @Test
  public void findIds() {

    DB.find(EColAB.class).delete();
    new EColAB("03", "someId").save();
    new EColAB("04", "someId").save();
    new EColAB("05", "someId").save();

    LoggedSql.start();

    List<Integer> colA_first = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.ON)
      .where()
      .eq("columnB", "someId")
      .findIds();

    List<Integer> colA_second = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.ON)
      .where()
      .eq("columnB", "someId")
      .findIds();

    List<String> sql = LoggedSql.stop();

    assertThat(colA_first).isSameAs(colA_second);
    assertThat(colA_first).hasSize(3);
    assertThat(sql).hasSize(1);

    // and now, ensure that we hit the database
    LoggedSql.start();
    colA_second = DB.find(EColAB.class)
      .setUseQueryCache(CacheMode.PUT)
      .where()
      .eq("columnB", "someId")
      .findIds();
    sql = LoggedSql.stop();

    assertThat(sql).hasSize(1);
  }

  @Test
  public void findCountDifferentQueriesBit() {
    DB.getDefault().pluginApi().cacheManager().clearAll();
    differentFindCount(q -> q.bitwiseAny("id", 1), q -> q.bitwiseAny("id", 0));
    differentFindCount(q -> q.bitwiseAll("id", 1), q -> q.bitwiseAll("id", 0));
    // differentFindCount(q->q.bitwiseNot("id",1), q->q.bitwiseNot("id",0)); NOT 1 == AND 1 = 0
    differentFindCount(q -> q.bitwiseAnd("id", 1, 0), q -> q.bitwiseAnd("id", 1, 1));

    differentFindCount(q -> q.bitwiseAnd("id", 2, 0), q -> q.bitwiseAnd("id", 4, 0));
    differentFindCount(q -> q.bitwiseAnd("id", 2, 1), q -> q.bitwiseAnd("id", 4, 1));
    // Will produce hash collision
    differentFindCount(q -> q.bitwiseAnd("id", 10, 0), q -> q.bitwiseAnd("id", 0, 928210));

  }

  void differentFindCount(Consumer<ExpressionList<EColAB>> q0, Consumer<ExpressionList<EColAB>> q1) {
    LoggedSql.start();

    ExpressionList<EColAB> el0 = DB.find(EColAB.class).setUseQueryCache(CacheMode.ON).where();
    q0.accept(el0);
    el0.findCount();

    ExpressionList<EColAB> el1 = DB.find(EColAB.class).setUseQueryCache(CacheMode.ON).where();
    q1.accept(el1);
    el1.findCount();

    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(2); // different queries
  }

}
