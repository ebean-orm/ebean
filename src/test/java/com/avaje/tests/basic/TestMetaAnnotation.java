package com.avaje.tests.basic;

import javax.persistence.PersistenceException;

import org.junit.Ignore;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Address;
import com.avaje.tests.model.basic.metaannotation.SizeMedium;
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
    Ebean.createUpdate(Address.class, "delete from address");

    Address address = new Address();
    address.setLine1(spaces100);
    Ebean.save(address);
  }

  /**
   * This test writes 101 spaces to "line1" which is annotated with &#64;Size(max=100).
   */
  @Test(expected=PersistenceException.class)
  @Ignore // Unfortunately, this test does not work with PostGres as DB-Backend, as it does not honor the @Size annotation 
  public void testWrite101SpacesToLine1() {
    Ebean.createUpdate(Address.class, "delete from address");

    Address address = new Address();
    address.setLine1(spaces101);
    Ebean.save(address);
  }

  /**
   * This test writes 100 spaces to "line1" which is meta-annotated with {@link SizeMedium}. 
   */
  @Test
  public void testWrite100SpacesToLine2() {
    Ebean.createUpdate(Address.class, "delete from address");

    Address address = new Address();
    address.setLine2(spaces100);
    Ebean.save(address);
  }

  /**
   * This test writes 101 spaces to "line1" which is meta-annotated with {@link SizeMedium}.
   */
  @Test(expected=PersistenceException.class)
  @Ignore // Unfortunately, this test does not work with PostGres as Backend
  public void testWrite101SpacesToLine2() {
    Ebean.createUpdate(Address.class, "delete from address");

    Address address = new Address();
    address.setLine2(spaces101);
    Ebean.save(address);
  }

}
