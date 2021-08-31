package org.tests.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.tests.model.basic.EBasicLog;
import org.tests.model.basic.EBasicWithLog;

import io.ebean.BaseTestCase;
import io.ebean.DB;

public class TestLifecycleWithLog extends BaseTestCase {

  private List<String> getLogs() {
    List<String> ret = DB.find(EBasicLog.class)
        .select("name")
        .findSingleAttributeList();
    DB.find(EBasicLog.class).delete();
    return ret;
  }

  @Test
  public void testCUD() {

    EBasicWithLog bean = new EBasicWithLog();
    bean.setId(1L);
    bean.setName("Test1");

    DB.save(bean);
    assertThat(getLogs()).contains("prePersist", "postPersist");

    bean.setName("Test2");
    DB.save(bean);
    assertThat(getLogs()).contains("preUpdate", "postUpdate");

    DB.delete(bean);
    assertThat(getLogs()).contains("preSoftDelete", "postSoftDelete");

    DB.deletePermanent(bean);
    assertThat(getLogs()).contains("preRemove", "postRemove");
  }

  @Test
  public void testCUDBatch() {

    EBasicWithLog bean = new EBasicWithLog();
    bean.setId(2L);
    bean.setName("Test2");

    List<EBasicWithLog> beans = Arrays.asList(bean);

    DB.saveAll(beans);
    assertThat(getLogs()).contains("prePersist", "postPersist");

    bean.setName("Test2Modified");
    DB.saveAll(beans);
    assertThat(getLogs()).contains("preUpdate", "postUpdate");

    DB.deleteAll(beans);
    assertThat(getLogs()).contains("preSoftDelete", "postSoftDelete");

    DB.deleteAllPermanent(beans);
    assertThat(getLogs()).contains("preRemove", "postRemove");
  }

}
