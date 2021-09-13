package org.tests.inheritance.cache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.cache.CInhOne;
import org.tests.model.basic.cache.CInhRef;
import org.tests.model.basic.cache.CInhRoot;
import org.tests.model.basic.cache.CInhTwo;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInheritanceRefCache extends BaseTestCase {

  @Test
  public void test() {

    CInhOne one = new CInhOne();
    one.setLicenseNumber("O12");
    one.setDriver("Jimmy");
    one.setNotes("Hello");

    DB.save(one);

    CInhRef ref = new CInhRef();
    ref.setRef(one);

    DB.save(ref);

    Integer id = ref.getId();

    LoggedSql.start();
    CInhRef gotRef = DB.find(CInhRef.class).setId(id).findOne();

    assertThat(gotRef).isInstanceOf(CInhRef.class);
    assertThat(gotRef.getRef()).isInstanceOf(CInhOne.class);
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("from cinh_ref").contains("left join cinh_root");

    // fetch again - from cache (but fetch second bean from cache)
    LoggedSql.start();
    gotRef = DB.find(CInhRef.class).setId(id).findOne();

    assertThat(gotRef).isInstanceOf(CInhRef.class);
    assertThat(gotRef.getRef()).isInstanceOf(CInhOne.class);
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(0);


    // fetch again - both from cache
    LoggedSql.start();
    gotRef = DB.find(CInhRef.class).setId(id).findOne();

    assertThat(gotRef).isInstanceOf(CInhRef.class);
    assertThat(gotRef.getRef()).isInstanceOf(CInhOne.class);
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(0);

  }

  @Test
  public void testMap() {

    List<Integer> ids = new ArrayList<>();

    CInhOne one = new CInhOne();
    one.setLicenseNumber("O19");
    one.setDriver("Foo");
    one.setNotes("Hello");
    DB.save(one);

    ids.add(one.getId());

    CInhTwo two = new CInhTwo();
    two.setLicenseNumber("T23");
    two.setAction("Test");
    DB.save(two);

    ids.add(two.getId());


    LoggedSql.start();

    DB.find(CInhRoot.class).setUseQueryCache(true).where().idIn(ids).setMapKey("licenseNumber").findMap();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("from cinh_root");

    // try some cache finds
    DB.find(CInhRoot.class).setUseQueryCache(true).findList();
    DB.find(CInhRoot.class).setUseQueryCache(true).findList();
    DB.find(CInhRoot.class).setUseQueryCache(true).findList();

    LoggedSql.start();

    DB.find(CInhRoot.class).setUseQueryCache(true).where().idIn(ids).setMapKey("licenseNumber").findMap();

    sql = LoggedSql.stop();
    assertThat(sql).hasSize(0);
  }

}
