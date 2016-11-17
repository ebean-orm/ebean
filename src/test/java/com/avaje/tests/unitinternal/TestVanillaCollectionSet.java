package com.avaje.tests.unitinternal;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.tests.model.basic.EVanillaCollection;
import com.avaje.tests.model.basic.EVanillaCollectionDetail;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestVanillaCollectionSet extends BaseTestCase {

  @Test
  public void test() {

    EVanillaCollection c = new EVanillaCollection();

    Ebean.save(c);

    EVanillaCollection c2 = Ebean.find(EVanillaCollection.class, c.getId());

    Assert.assertNotNull(c2);

    List<EVanillaCollectionDetail> details = c2.getDetails();
    Assert.assertNotNull(details);

    Assert.assertTrue("Is BeanCollection", details instanceof BeanCollection<?>);

    BeanCollection<?> bc = (BeanCollection<?>) details;
    Assert.assertTrue(!bc.isPopulated());
    Assert.assertNotNull(bc.getOwnerBean());
    Assert.assertNotNull(bc.getPropertyName());
  }

}
