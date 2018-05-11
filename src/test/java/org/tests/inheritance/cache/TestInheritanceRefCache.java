package org.tests.inheritance.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.cache.CInhOne;
import org.tests.model.basic.cache.CInhRef;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
