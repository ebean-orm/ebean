package org.tests.enhancement;

import io.ebean.BaseTestCase;
import org.tests.model.basic.Product;
import org.junit.Assert;
import org.junit.Test;

/**
 * Note take care if you run this test in an IDE with a debugger as that can trigger the equals and
 * hashCode (and the identity value can get populated un-normally).
 */
public class TestEnhancementEquals extends BaseTestCase {


  @Test
  public void testEqualsBasedOnIdValue() {

    Product product1 = new Product();
    product1.setId(345);
    product1.setName("blah");

    Product product2 = new Product();
    product2.setId(345);

    Assert.assertEquals("equal based on id", product1, product2);
    Assert.assertEquals("hashCode equal based on id", product1.hashCode(), product2.hashCode());

    product2.setName("kumera");
    Assert.assertEquals("still equal based on identity", product1, product2);
    Assert.assertEquals("still hashCode equal based on id", product1.hashCode(), product2.hashCode());
  }


  @Test
  public void testWhenEqualsTouchedFirst() {

    Product product1 = new Product();
    product1.setName("blah");
    int hashcode1 = product1.hashCode();

    // equals has been called so the hashCode and 'identity' has been baked in
    product1.setId(345);
    int hashcode2 = product1.hashCode();

    Assert.assertEquals("hashCode can't change", hashcode1, hashcode2);

    Product product2 = new Product();
    product2.setId(345);

    Assert.assertFalse("Not equal now", product1.equals(product2));
    Assert.assertTrue("Different hashCode", product1.hashCode() != product2.hashCode());
  }

}
