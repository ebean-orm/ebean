package org.avaje.test.model.inheritmany.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.tests.model.inheritmany.IMRelated;
import com.avaje.tests.model.inheritmany.IMRoot;
import com.avaje.tests.model.inheritmany.IMRootOne;
import com.avaje.tests.model.inheritmany.IMRootTwo;


public class TestInheritWithMany extends BaseTestCase {


  @Test
  public void test() {

    EbeanServer server = Ebean.getServer(null);
    Assert.assertNotNull(server);
    
    SpiEbeanServer spiServer = (SpiEbeanServer)server;
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
