package org.tests.cascade;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tests.model.site.DataContainer;
import org.tests.model.site.Site;
import org.tests.model.site.SiteAddress;

public class TestMultiCascadeBatch extends BaseTestCase {

  private Transaction txn;

  @Before
  public void before() {
    txn = Ebean.beginTransaction();
  }

  @After
  public void after() {
    if (txn != null) {
      txn.end();
    }
  }

  @Test
  public void testMultipleCascadeInsideTransaction() {

    final Site mainSite = new Site();
    mainSite.setName("mainSite");
    Ebean.save(mainSite);

    // create child including data
    final Site childSite = new Site();
    childSite.setName("childSite");

    final SiteAddress childAddress = new SiteAddress();
    childAddress.setCity("Some city");
    childAddress.setStreet("some street");
    childAddress.setZipCode("12345");
    childSite.setSiteAddress(childAddress);

    final DataContainer dataContainer = new DataContainer();
    dataContainer.setContent("container content");
    childSite.setDataContainer(dataContainer);

    mainSite.getChildren().add(childSite);
    mainSite.setName("a different name");

    Ebean.save(mainSite);
  }

}
