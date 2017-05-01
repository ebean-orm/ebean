package org.tests.cascade;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.bean.BeanCollection;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestPrivateOwned extends BaseTestCase {

  @Test
  public void test() {

    TSMaster m0 = new TSMaster();
    m0.setName("m1");

    m0.addDetail(new TSDetail("m1 detail 1"));
    m0.addDetail(new TSDetail("m1 detail 2"));

    Ebean.save(m0);

    TSMaster master = Ebean.find(TSMaster.class, m0.getId());
    List<TSDetail> details = master.getDetails();

    TSDetail removedDetail = details.remove(1);

    BeanCollection<?> bc = (BeanCollection<?>) details;
    Set<?> modifyRemovals = bc.getModifyRemovals();

    assertNotNull(modifyRemovals);
    assertTrue(modifyRemovals.size() == 1);
    assertTrue(modifyRemovals.contains(removedDetail));

    Ebean.save(master);

    TSMaster masterReload = Ebean.find(TSMaster.class, m0.getId());
    List<TSDetail> detailsReload = masterReload.getDetails();

    // the removed bean has really been removed
    assertTrue(detailsReload.size() == 1);

    TSMaster master3 = Ebean.find(TSMaster.class, m0.getId());
    List<TSDetail> details3 = master3.getDetails();
    details3.clear();
  }


  @Test
  public void insertThenUpdate() {

    TSMaster m0 = new TSMaster();
    m0.setName("m2");

    m0.addDetail(new TSDetail("m2 detail 1"));
    m0.addDetail(new TSDetail("m2 detail 2"));

    Ebean.save(m0);
    assertThat(m0.getDetails()).hasSize(2);

    LoggedSqlCollector.start();
    m0.getDetails().remove(0);
    Ebean.save(m0);

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(loggedSql.get(0)).contains("delete from t_detail_with_other_namexxxyy where id=?");

    TSMaster masterReload = Ebean.find(TSMaster.class, m0.getId());
    assertThat(masterReload.getDetails()).hasSize(1);

    LoggedSqlCollector.start();
    Ebean.save(m0);
    loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(0);

  }

}
