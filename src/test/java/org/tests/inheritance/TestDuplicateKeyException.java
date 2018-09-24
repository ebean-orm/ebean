package org.tests.inheritance;


import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.basic.AttributeHolder;
import org.tests.model.basic.ListAttribute;
import org.tests.model.basic.ListAttributeValue;

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

    Ebean.save(value1);

    final ListAttribute listAttribute = new ListAttribute();
    listAttribute.add(value1);
    Ebean.save(listAttribute);


    final AttributeHolder holder = new AttributeHolder();
    holder.add(listAttribute);

    try {
      Ebean.execute(() -> {
        //Ebean.currentTransaction().log("-- saving holder first time");
        // Alternatively turn off cascade Persist for this transaction
        //Ebean.currentTransaction().setPersistCascade(false);
        Ebean.save(holder);
//Ebean.currentTransaction().log("-- saving holder second time");
        // we don't get this far before failing
        //Ebean.save(holder);
      });
    } catch (Exception e) {
      Assert.assertEquals(e.getMessage(), "test rollback");
    }
  }
}
