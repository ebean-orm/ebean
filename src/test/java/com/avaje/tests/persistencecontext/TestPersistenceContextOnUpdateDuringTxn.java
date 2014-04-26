package com.avaje.tests.persistencecontext;

import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.tests.model.basic.EBasic;

public class TestPersistenceContextOnUpdateDuringTxn extends BaseTestCase {

  @Test
  public void test() {
    
    EbeanServer server = Ebean.getServer(null);
    
    server.beginTransaction();
    try {
      
      EBasic bean1 = new EBasic();
      bean1.setName("hello");
      
      server.save(bean1);
      
      EBasic updatedEntity = new EBasic();
      updatedEntity.setId(bean1.getId());
      updatedEntity.setName("hello-changed");
      
      server.update(updatedEntity);
      
      // actually the bean is not in the persistence context so ...  the assert is fine
      EBasic loadedEntity = server.find(EBasic.class,bean1.getId());

      assertThat(loadedEntity.getName(), is("hello-changed")); 

    } finally {
      server.endTransaction();
    }
    
  }
  
}
