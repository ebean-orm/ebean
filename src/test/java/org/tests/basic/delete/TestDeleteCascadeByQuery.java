package org.tests.basic.delete;

import static org.assertj.core.api.Assertions.assertThat;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;

import org.junit.Test;

import org.tests.model.onetoone.OtoUser;
import org.tests.model.onetoone.OtoUserOptional;

public class TestDeleteCascadeByQuery extends BaseTestCase {
  
  Query<OtoUserOptional> userOptionalQuery = Ebean.find(OtoUserOptional.class);
  Query<OtoUser> userQuery = Ebean.find(OtoUser.class);
  
  /**
   * Test that validates deleting a bean with OneToOne mapping with a query respects the 
   * CascadeType.DELETE setting.
   */
  @Test
  public void testDeleteCascadeByQuery() {

    // Check that both models do not exist
    assertNoUserOrOptionalExist();

    // Create OtoUser and assign OtoUserOptional to it then save
    createUserAndOptional();
    
    // Deleting the testUser using Ebean.delete() works
    // Ebean.delete(testUser);
    
    // Deleting the testUser using the Ebean query
    assertThat(userQuery.delete()).isEqualTo(1);
    
    // Check that both models are deleted (CascadeType.DELETE was respected)
    assertNoUserOrOptionalExist();

  }
  
  private OtoUser createUserAndOptional() {
    OtoUserOptional userOptional = new OtoUserOptional();
    OtoUser testUser = new OtoUser();
    testUser.setOptional(userOptional);
    Ebean.save(userOptional);
    Ebean.save(testUser);
    return testUser;
  }
  
  private void assertNoUserOrOptionalExist() {
    assertThat(userOptionalQuery.findCount())
      .overridingErrorMessage("Entity OtoUserOptional found.")
      .isEqualTo(0);
    assertThat(userQuery.findCount())
      .overridingErrorMessage("Entity OtoUser found.")
      .isEqualTo(0);
  }
}
