package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.AttributeHolder;
import org.tests.model.basic.ListAttribute;
import org.tests.model.basic.ListAttributeValue;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSkippable extends BaseTestCase {

  private static final Logger logger = LoggerFactory.getLogger(TestSkippable.class);

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
    final ListAttributeValue value2 = new ListAttributeValue();

    Ebean.save(value1);
    Ebean.save(value2);

    final ListAttribute listAttribute = new ListAttribute();
    listAttribute.add(value1);
    Ebean.save(listAttribute);
    logger.info(" -- seeded data");

    final ListAttribute listAttributeDB = Ebean.find(ListAttribute.class, listAttribute.getId());
    Assert.assertNotNull(listAttributeDB);

    final ListAttributeValue value1_DB = listAttributeDB.getValues().iterator().next();
    Assert.assertTrue(value1.getId().equals(value1_DB.getId()));
    logger.info(" -- asserted data in db");


    final AttributeHolder holder = new AttributeHolder();
    holder.add(listAttributeDB);

    Ebean.save(holder);
    logger.info(" -- saved holder");

    // Now change the M2M listAttribute.values and save the holder
    // The save should cascade as follows
    // holder.attributes..ListAttribute.values
    listAttributeDB.getValues().clear();
    listAttributeDB.add(value2);

    // Save the holder - should cascade down to the listAtribute and save the values
    Ebean.save(holder);
    logger.info(" -- M2M detected delete of value1 and add of value2 ?");


    final ListAttribute listAttributeDB_2 = Ebean.find(ListAttribute.class, listAttributeDB.getId());
    Assert.assertNotNull(listAttributeDB_2);
    final ListAttributeValue value2_DB_2 = listAttributeDB_2.getValues().iterator().next();


    Assert.assertEquals(value2.getId(), value2_DB_2.getId());
    Assert.assertTrue("Cascade failed", value2.getId().equals(value2_DB_2.getId()));

  }
}
