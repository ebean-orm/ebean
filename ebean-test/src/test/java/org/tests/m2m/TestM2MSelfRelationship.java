package org.tests.m2m;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.m2m.MnyTopic;

/**
 * Added to test DDL generation for ManyToMany related back to itself.
 */
public class TestM2MSelfRelationship extends BaseTestCase {

  @Test
  public void test() {

    DB.getDefault();

    // Create 2 roles r0 and r1
    MnyTopic r0 = new MnyTopic("r0");
    MnyTopic r1 = new MnyTopic("r1");

    // Save r1 and r2
    DB.save(r0);
    DB.save(r1);

    r0.getSubTopics().add(r1);
    DB.save(r0);
  }
}
