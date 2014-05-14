package com.avaje.tests.autofetch;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.embedded.EMain;
import com.avaje.tests.model.embedded.Eembeddable;


public class AutofetchEmbeddedTest extends BaseTestCase {

  private final Logger logger = LoggerFactory.getLogger(AutofetchEmbeddedTest.class);

  @Test
  public void testEmbeddedBeanLazyLoadAndUpdate() {
    
    EMain testBean = new EMain();
    testBean.setName("test");
    testBean.getEmbeddable().setDescription("test description");
    Ebean.save(testBean);

    EMain partialBean = Ebean.find(EMain.class).select("version").setId(testBean.getId()).findUnique();
    
    logger.info(" -- invoke lazy loading of embedded bean");
    Eembeddable embeddable = partialBean.getEmbeddable();
    embeddable.setDescription("modified description");
    
    logger.info(" -- update bean");
    Ebean.save(partialBean);
    
  }
  
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

  @Test
  public void testEmbeddedFetch() {
    Ebean.find(EMain.class).fetch("embeddable").findList();
  }
}
