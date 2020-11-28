package org.tests.test;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.InheritInfo;
import org.tests.model.inheritmany.IMRelated;
import org.tests.model.inheritmany.IMRoot;
import org.tests.model.inheritmany.IMRootOne;
import org.tests.model.inheritmany.IMRootTwo;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;


public class TestInheritWithMany extends BaseTestCase {


  @Test
  public void test() {

    EbeanServer server = Ebean.getServer(null);
    Assert.assertNotNull(server);

    SpiEbeanServer spiServer = (SpiEbeanServer) server;
    BeanDescriptor<IMRoot> beanDescriptor = spiServer.getBeanDescriptor(IMRoot.class);
    InheritInfo inheritInfo = beanDescriptor.getInheritInfo();
    Assert.assertNotNull(inheritInfo);


    IMRootOne one = new IMRootOne();
    one.setName("One Name");
    server.save(one);
    add(one, "aaa");
    add(one, "bbb");


    IMRootTwo two = new IMRootTwo();
    two.setTitle("Two Title");
    server.save(two);
    add(two, "ccc");
    add(two, "ddd");


    List<IMRoot> list = server.find(IMRoot.class).select("id").findList();

    for (IMRoot imRoot : list) {
      // lazy load the related OneToMany which is related to a non-leaf
      List<IMRelated> related = imRoot.getRelated();
      for (IMRelated imRelated : related) {
        imRelated.getName();
      }
    }

  }

  private void add(IMRoot owner, String string) {

    IMRelated relate = new IMRelated();
    relate.setName(string);
    relate.setOwner(owner);

    EbeanServer server = Ebean.getServer(null);
    server.save(relate);
  }


}
