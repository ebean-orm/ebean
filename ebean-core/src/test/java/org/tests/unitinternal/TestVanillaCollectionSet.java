package org.tests.unitinternal;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.bean.BeanCollection;
import org.tests.model.basic.EVanillaCollection;
import org.tests.model.basic.EVanillaCollectionDetail;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestVanillaCollectionSet extends BaseTestCase {

  @Test
  public void test() {

    EVanillaCollection c = new EVanillaCollection();

    Ebean.save(c);

    EVanillaCollection c2 = Ebean.find(EVanillaCollection.class, c.getId());

    assertNotNull(c2);

    List<EVanillaCollectionDetail> details = c2.getDetails();
    assertNotNull(details);

    assertTrue(details instanceof BeanCollection<?>);

    BeanCollection<?> bc = (BeanCollection<?>) details;
    assertTrue(!bc.isPopulated());
    assertNotNull(bc.getOwnerBean());
    assertNotNull(bc.getPropertyName());
  }

}
