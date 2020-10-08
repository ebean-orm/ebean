package org.tests.model.selfref;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.junit.Test;

public class TestResourceFileSelfRef extends BaseTestCase {

  @Test
  public void test() {

    EbeanServer server = Ebean.getServer(null);

    ResourceFile childFile1 = new ResourceFile();
    childFile1.setName("childFile1");
    ResourceFile childFile2 = new ResourceFile();
    childFile2.setName("childFile2");

    ResourceFile parentFile1 = new ResourceFile();
    parentFile1.setName("parentFile1");

    childFile1.setParent(parentFile1);
    childFile2.setParent(parentFile1);
    parentFile1.getAlternatives().add(childFile1);
    parentFile1.getAlternatives().add(childFile2);

    server.save(parentFile1);
    server.save(childFile1);
    server.save(childFile2);

    // As a workaround for the problem, the child objects can be deleted first
    //server.delete(childFile1);
    //server.delete(childFile2);
    server.delete(parentFile1);
  }
}
