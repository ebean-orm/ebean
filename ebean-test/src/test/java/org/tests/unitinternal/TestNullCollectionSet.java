package org.tests.unitinternal;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.bean.BeanCollection;
import org.tests.model.basic.ENullCollection;
import org.tests.model.basic.ENullCollectionDetail;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestNullCollectionSet extends BaseTestCase {

  @Test
  public void test() {
    ENullCollection c = new ENullCollection();

    DB.save(c);

    ENullCollection c2 = DB.find(ENullCollection.class, c.getId());

    assertNotNull(c2);

    List<ENullCollectionDetail> details = c2.getDetails();
    assertNotNull(details);

    assertTrue(details instanceof BeanCollection<?>);

    BeanCollection<?> bc = (BeanCollection<?>) details;
    assertFalse(bc.isPopulated());
    assertNotNull(bc.getOwnerBean());
    assertNotNull(bc.getPropertyName());
  }

}
