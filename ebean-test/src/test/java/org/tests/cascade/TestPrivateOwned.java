package org.tests.cascade;

import io.ebean.DB;
import io.ebean.bean.BeanCollection;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.TSDetail;
import org.tests.model.basic.TSMaster;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPrivateOwned extends BaseTestCase {

  @Test
  public void test() {

    TSMaster m0 = new TSMaster();
    m0.setName("m1");

    m0.addDetail(new TSDetail("m1 detail 1"));
    m0.addDetail(new TSDetail("m1 detail 2"));

    DB.save(m0);

    TSMaster master = DB.find(TSMaster.class, m0.getId());
    List<TSDetail> details = master.getDetails();

    TSDetail removedDetail = details.remove(1);

    BeanCollection<?> bc = (BeanCollection<?>) details;
    Set<?> modifyRemovals = bc.modifyRemovals();

    assertNotNull(modifyRemovals);
    assertTrue(modifyRemovals.size() == 1);
    assertTrue(modifyRemovals.contains(removedDetail));

    DB.save(master);

    TSMaster masterReload = DB.find(TSMaster.class, m0.getId());
    List<TSDetail> detailsReload = masterReload.getDetails();

    // the removed bean has really been removed
    assertTrue(detailsReload.size() == 1);

    TSMaster master3 = DB.find(TSMaster.class, m0.getId());
    List<TSDetail> details3 = master3.getDetails();
    details3.clear();
  }


  @Test
  public void insertThenUpdate() {

    TSMaster m0 = new TSMaster();
    m0.setName("m2");

    m0.addDetail(new TSDetail("m2 detail 1"));
    m0.addDetail(new TSDetail("m2 detail 2"));

    DB.save(m0);
    assertThat(m0.getDetails()).hasSize(2);

    LoggedSql.start();
    m0.getDetails().remove(0);
    DB.save(m0);

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(3);
    assertThat(loggedSql.get(0)).contains("delete from t_detail_with_other_namexxxyy where id=?");
    assertSqlBind(loggedSql.get(1));

    TSMaster masterReload = DB.find(TSMaster.class, m0.getId());
    assertThat(masterReload.getDetails()).hasSize(1);

    LoggedSql.start();
    DB.save(m0);
    loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(0);

  }

}
