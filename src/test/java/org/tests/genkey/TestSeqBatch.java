package org.tests.genkey;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.config.dbplatform.IdType;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.tests.model.basic.TOne;
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
