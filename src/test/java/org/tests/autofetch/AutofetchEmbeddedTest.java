package org.tests.autofetch;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.embedded.EMain;
import org.tests.model.embedded.Eembeddable;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AutofetchEmbeddedTest extends BaseTestCase {

  private final Logger logger = LoggerFactory.getLogger(AutofetchEmbeddedTest.class);

  @Test
  public void testEmbeddedBeanLazyLoadAndUpdate() {

    EMain testBean = new EMain();
    testBean.setName("test");
    testBean.getEmbeddable().setDescription("test description");
    Ebean.save(testBean);

    EMain partialBean = Ebean.find(EMain.class).select("version").setId(testBean.getId()).findOne();

    logger.info(" -- invoke lazy loading of embedded bean");
    Eembeddable embeddable = partialBean.getEmbeddable();
    embeddable.setDescription("modified description");

    logger.info(" -- update bean");
    Ebean.save(partialBean);

  }

  @Test
  public void testEmbeddedBeanQueryTuning() {
//    Ebean.getServer(null).getAutoTune().setProfiling(true);
//    Ebean.getServer(null).getAutoTune().setQueryTuning(true);
//    Ebean.getServer(null).getAutoTune().setProfilingBase(1);
//
//    EMain testBean = new EMain();
//    testBean.setName("test");
//    testBean.getEmbeddable().setDescription("test description");
//    Ebean.save(testBean);
//
//    //This should not throw an exception
//    for (int i = 0; i < 5; i++) {
//      Ebean.beginTransaction();
//      try {
//        List<EMain> result = Ebean.find(EMain.class).setAutoTune(true).findList();
//        for (EMain e : result) {
//          e.getEmbeddable().setDescription("Test" + i);
//          Ebean.save(e);
//        }
//        Ebean.commitTransaction();
//      } finally {
//        Ebean.endTransaction();
//        logger.debug(Ebean.getServer(null).getAutoTune().collectProfiling());
//      }
//    }
  }

  @Test
  public void testEmbeddedFetch() {
    Ebean.find(EMain.class).fetch("embeddable").findList();
  }
}
