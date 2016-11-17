package com.avaje.tests.genkey;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.tests.model.basic.TOne;
import org.junit.Assert;
import org.junit.Test;

public class TestSeqBatch extends BaseTestCase {

  @Test
  public void test() {

    EbeanServer server = Ebean.getServer(null);
    SpiEbeanServer spiServer = (SpiEbeanServer) server;

    IdType idType = spiServer.getDatabasePlatform().getDbIdentity().getIdType();

    if (IdType.SEQUENCE == idType) {
      BeanDescriptor<TOne> d = spiServer.getBeanDescriptor(TOne.class);

      Object id = d.nextId(null);
      Assert.assertNotNull(id);
      //System.out.println(id);

      for (int i = 0; i < 16; i++) {
        Object id2 = d.nextId(null);
        Assert.assertNotNull(id2);
        //System.out.println(id2);
      }
    }
  }

}
