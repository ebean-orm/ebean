package org.tests.unitinternal;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.bean.BeanCollection;
import org.tests.model.basic.EVanillaCollection;
import org.tests.model.basic.EVanillaCollectionDetail;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestVanillaCollectionSet extends BaseTestCase {

  @Test
  public void test() {

    EVanillaCollection c = new EVanillaCollection();

    DB.save(c);

    EVanillaCollection c2 = DB.find(EVanillaCollection.class, c.getId());

    assertNotNull(c2);

    List<EVanillaCollectionDetail> details = c2.getDetails();
    assertNotNull(details);

    assertTrue(details instanceof BeanCollection<?>);

    BeanCollection<?> bc = (BeanCollection<?>) details;
    assertFalse(bc.isPopulated());
    assertNotNull(bc.getOwnerBean());
    assertNotNull(bc.getPropertyName());
  }

}
