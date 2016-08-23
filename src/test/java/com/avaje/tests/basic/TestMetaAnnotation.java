package com.avaje.tests.basic;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.metaannotation.SizeMedium;
import org.junit.Test;

import javax.persistence.PersistenceException;

import static org.junit.Assert.assertTrue;

/**
 * Very simple test case to check if Ebean recognizes meta-annotation correctly.
 * 
 * @author Roland Praml, FOCONIS AG
 */
public class TestMetaAnnotation extends BaseTestCase {

  private static final String spaces100 = new String(new char[100]).replace('\0', ' '); 
  private static final String spaces101 = new String(new char[101]).replace('\0', ' '); 
	
  /**
   * This test writes 100 spaces to "line1" which is annotated with &#64;Size(max=100) 
   */
  @Test
  public void testWrite100SpacesToLine1() {

    Address address = new Address();
    address.setLine1(spaces100);
    Ebean.save(address);
  }

  /**
   * This test writes 100 spaces to "line1" which is meta-annotated with {@link SizeMedium}.
   */
  @Test
  public void testWrite100SpacesToLine2() {

    Address address = new Address();
    address.setLine2(spaces100);
    Ebean.save(address);
  }

  /**
   * This test writes 101 spaces to "line1" which is annotated with &#64;Size(max=100).
   */
  @Test
  public void testWrite101SpacesToLine1() {

    if (isH2()) {
      Address address = new Address();
      address.setLine1(spaces101);
      try {
        Ebean.save(address);
        assertTrue("H2 fails this insert", false);
      } catch (PersistenceException e) {
        assertTrue(true);
      }
    }
  }

  /**
   * This test writes 101 spaces to "line1" which is meta-annotated with {@link SizeMedium}.
   */
  @Test
  public void testWrite101SpacesToLine2() {

    if (isH2()) {
      Address address = new Address();
      address.setLine2(spaces101);
      try {
        Ebean.save(address);
        assertTrue("H2 fails this insert", false);
      } catch (PersistenceException e) {
        assertTrue(true);
      }
    }
  }

}
