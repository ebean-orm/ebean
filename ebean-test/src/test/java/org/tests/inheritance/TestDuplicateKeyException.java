package org.tests.inheritance;


import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Attribute;
import org.tests.model.basic.AttributeHolder;
import org.tests.model.basic.ListAttributeValue;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDuplicateKeyException extends BaseTestCase {

  /**
   * Test query.
   * <p>This test covers the BUG 276. Cascade was not propagating to the ListAttribute because
   * it was considered safe to skip as it didn't take into account any derived classes
   * into account with e.g. collections and Cascade options </p>
   */
  @Test
  public void testQuery() {

    // Setup the data first
    final ListAttributeValue value1 = new ListAttributeValue();

    DB.save(value1);

    final Attribute listAttribute = new Attribute();
    listAttribute.add(value1);
    DB.save(listAttribute);


    final AttributeHolder holder = new AttributeHolder();
    holder.add(listAttribute);

    try {
      DB.execute(() -> {
        //DB.currentTransaction().log("-- saving holder first time");
        // Alternatively turn off cascade Persist for this transaction
        //DB.currentTransaction().setPersistCascade(false);
        DB.save(holder);
//DB.currentTransaction().log("-- saving holder second time");
        // we don't get this far before failing
        //DB.save(holder);
      });
    } catch (Exception e) {
      assertEquals(e.getMessage(), "test rollback");
    }
  }
}
