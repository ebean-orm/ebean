package org.tests.basic.delete;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.model.onetoone.OtoUser;
import org.tests.model.onetoone.OtoUserOptional;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeleteCascadeByQuery extends BaseTestCase {

  private OtoUser testUser;
  private OtoUserOptional userOptional;
  private Query<OtoUserOptional> userOptionalQuery = DB.find(OtoUserOptional.class);
  private Query<OtoUser> userQuery = DB.find(OtoUser.class);

  /**
   * Init each test. Delete all existing beans. Then create OtoUser, add OtoUserOptional and save.
   */
  @BeforeEach
  public void init() {
    DB.deleteAll(userQuery.findList());
    DB.deleteAll(userOptionalQuery.findList());

    userOptional = new OtoUserOptional();
    DB.save(userOptional);
    testUser = new OtoUser();
    testUser.setOptional(userOptional);
    DB.save(testUser);
  }

  /**
   * Test that validates deleting a bean using DB.delete() respects the CascadeType.DELETE
   * setting.
   */
  @Test
  public void testDeleteCascadeByEbeanDelete() {

    assertThat(DB.delete(testUser)).isTrue();

    assertThat(userOptionalQuery.findCount())
      .overridingErrorMessage("Entity OtoUserOptional found. DB.delete() on the user "
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
  @AfterEach
  public void cleanup() {
    DB.deleteAll(userQuery.findList());
    DB.deleteAll(userOptionalQuery.findList());
  }
}
