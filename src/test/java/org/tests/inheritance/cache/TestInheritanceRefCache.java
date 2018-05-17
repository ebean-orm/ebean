package org.tests.inheritance.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.cache.CInhOne;
import org.tests.model.basic.cache.CInhRef;
import org.tests.model.basic.cache.CInhRoot;
import org.tests.model.basic.cache.CInhTwo;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.ebeantest.LoggedSqlCollector;

public class TestInheritanceRefCache extends BaseTestCase {

  @Test
  public void test() {

    CInhOne one = new CInhOne();
    one.setLicenseNumber("O12");
    one.setDriver("Jimmy");
    one.setNotes("Hello");

    Ebean.save(one);

    CInhRef ref = new CInhRef();
    ref.setRef(one);

    Ebean.save(ref);

    Integer id = ref.getId();

    LoggedSqlCollector.start();
    CInhRef gotRef = Ebean.find(CInhRef.class).setId(id).findOne();

    assertThat(gotRef).isInstanceOf(CInhRef.class);
    assertThat(gotRef.getRef()).isInstanceOf(CInhOne.class);
    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("from cinh_ref").contains("left join cinh_root");

    // fetch again - from cache (but fetch second bean from cache)
    LoggedSqlCollector.start();
    gotRef = Ebean.find(CInhRef.class).setId(id).findOne();

    assertThat(gotRef).isInstanceOf(CInhRef.class);
    assertThat(gotRef.getRef()).isInstanceOf(CInhOne.class);
    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("from cinh_root");


    // fetch again - both from cache
    LoggedSqlCollector.start();
    gotRef = Ebean.find(CInhRef.class).setId(id).findOne();

    assertThat(gotRef).isInstanceOf(CInhRef.class);
    assertThat(gotRef.getRef()).isInstanceOf(CInhOne.class);
    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(0);

  }

  @Test
  public void testMap() {

    List<Integer> ids = new ArrayList<>();

    CInhOne one = new CInhOne();
    one.setLicenseNumber("O19");
    one.setDriver("Foo");
    one.setNotes("Hello");
    Ebean.save(one);

    ids.add(one.getId());

    CInhTwo two = new CInhTwo();
    two.setLicenseNumber("T23");
    two.setAction("Test");
    Ebean.save(two);

    ids.add(two.getId());


    LoggedSqlCollector.start();

    Ebean.find(CInhRoot.class).setUseQueryCache(true).where().idIn(ids).setMapKey("licenseNumber").findMap();

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("from cinh_root");

    // try some cache finds
    Ebean.find(CInhRoot.class).setUseQueryCache(true).findList();
    Ebean.find(CInhRoot.class).setUseQueryCache(true).findList();
    Ebean.find(CInhRoot.class).setUseQueryCache(true).findList();

    LoggedSqlCollector.start();

    Ebean.find(CInhRoot.class).setUseQueryCache(true).where().idIn(ids).setMapKey("licenseNumber").findMap();

    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(0);
  }

}
