package org.tests.basic.delete;

import static org.assertj.core.api.Assertions.assertThat;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.tests.model.onetoone.OtoUser;
import org.tests.model.onetoone.OtoUserOptional;

public class TestDeleteCascadeByQuery extends BaseTestCase {
  
  private OtoUser testUser;
  private OtoUserOptional userOptional;
  private Query<OtoUserOptional> userOptionalQuery = Ebean.find(OtoUserOptional.class);
  private Query<OtoUser> userQuery = Ebean.find(OtoUser.class);
  
  /**
   * Init each test. Delete all existing beans. Then create OtoUser, add OtoUserOptional and save.
   */
  @Before
  public void init() {
    Ebean.deleteAll(userQuery.findList());
    Ebean.deleteAll(userOptionalQuery.findList());
    
    userOptional = new OtoUserOptional();
    Ebean.save(userOptional);
    testUser = new OtoUser();
    testUser.setOptional(userOptional);
    Ebean.save(testUser);
  }
  
  /**
   * Test that validates deleting a bean using Ebean.delete() respects the CascadeType.DELETE 
   * setting.
   */
  @Test
  public void testDeleteCascadeByEbeanDelete() {

    assertThat(Ebean.delete(testUser)).isTrue();

    assertThat(userOptionalQuery.findCount())
      .overridingErrorMessage("Entity OtoUserOptional found. Ebean.delete() on the user "
          + "did not delete the OneToOne mapped entity as set with CascadeType.ALL")
      .isEqualTo(0);
  }
  
  /**
   * Test that validates deleting a bean with OneToOne mapping with a query respects the 
   * CascadeType.DELETE setting.
   */
  @Test
  public void testDeleteCascadeByQuery() {
    
    assertThat(userQuery.delete()).isEqualTo(1);

    assertThat(userOptionalQuery.findCount())
      .overridingErrorMessage("Entity OtoUserOptional found. Ebean query delete() on the user "
          + "did not delete the OneToOne mapped entity as set with CascadeType.ALL")
      .isEqualTo(0);
  }
  
  /**
   * Cleanup - Delete all existing beans for the next test.
   */
  @After
  public void cleanup() {
    Ebean.deleteAll(userQuery.findList());
    Ebean.deleteAll(userOptionalQuery.findList());
  }
}
