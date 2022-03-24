package org.tests.enhancement;

import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Product;

import static org.junit.jupiter.api.Assertions.*;

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

    assertEquals(product1, product2);
    assertEquals(product1.hashCode(), product2.hashCode());

    product2.setName("kumera");
    assertEquals(product1, product2);
    assertEquals(product1.hashCode(), product2.hashCode());
  }


  @Test
  public void testWhenEqualsTouchedFirst() {

    Product product1 = new Product();
    product1.setName("blah");
    int hashcode1 = product1.hashCode();

    // equals has been called so the hashCode and 'identity' has been baked in
    product1.setId(345);
    int hashcode2 = product1.hashCode();

    assertEquals(hashcode1, hashcode2);

    Product product2 = new Product();
    product2.setId(345);

    assertNotEquals(product1, product2);
    assertTrue(product1.hashCode() != product2.hashCode());
  }

}
