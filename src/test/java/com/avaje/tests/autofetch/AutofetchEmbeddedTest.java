package com.avaje.tests.autofetch;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.embedded.EMain;
import org.junit.Test;
import java.util.List;


public class AutofetchEmbeddedTest extends BaseTestCase {

  @Test
  public void testEmbeddedBeanQueryTuning() {
    Ebean.getServer(null).getAdminAutofetch().setProfiling(true);
    Ebean.getServer(null).getAdminAutofetch().setQueryTuning(true);
    Ebean.getServer(null).getAdminAutofetch().setProfilingBase(1);

    EMain testBean = new EMain();
    testBean.setName("test");
    testBean.getEmbeddable().setDescription("test description");
    Ebean.save(testBean);

    //This should not throw an exception
    for (int i = 0; i < 5; i++) {
      Ebean.beginTransaction();
      try {
        List<EMain> result = Ebean.find(EMain.class).setAutofetch(true).findList();
        for (EMain e : result) {
          e.getEmbeddable().setDescription("Test" + i);
          Ebean.save(e);
        }
        Ebean.commitTransaction();
      } finally {
        Ebean.endTransaction();
        logger.debug(Ebean.getServer(null).getAdminAutofetch().collectUsageViaGC());
      }
    }
  }
}
