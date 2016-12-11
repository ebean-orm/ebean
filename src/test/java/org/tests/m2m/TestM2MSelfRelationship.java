package org.tests.m2m;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.m2m.MnyTopic;
import org.junit.Test;

/**
 * Added to test DDL generation for ManyToMany related back to itself.
 */
public class TestM2MSelfRelationship extends BaseTestCase {

  @Test
  public void test() {

    Ebean.getDefaultServer();

    // Create 2 roles r0 and r1
    MnyTopic r0 = new MnyTopic("r0");
    MnyTopic r1 = new MnyTopic("r1");

    // Save r1 and r2
    Ebean.save(r0);
    Ebean.save(r1);

    r0.getSubTopics().add(r1);
    Ebean.save(r0);
  }
}
