package org.tests.unitinternal;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.bean.BeanCollection;
import org.tests.model.basic.ENullCollection;
import org.tests.model.basic.ENullCollectionDetail;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestNullCollectionSet extends BaseTestCase {

  @Test
  public void test() {

    ENullCollection c = new ENullCollection();

    Ebean.save(c);

    ENullCollection c2 = Ebean.find(ENullCollection.class, c.getId());

    Assert.assertNotNull(c2);

    List<ENullCollectionDetail> details = c2.getDetails();
    Assert.assertNotNull(details);

    Assert.assertTrue("Is BeanCollection", details instanceof BeanCollection<?>);

    BeanCollection<?> bc = (BeanCollection<?>) details;
    Assert.assertTrue(!bc.isPopulated());
    Assert.assertNotNull(bc.getOwnerBean());
    Assert.assertNotNull(bc.getPropertyName());
  }

}
