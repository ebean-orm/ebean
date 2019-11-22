package org.tests.genkey;

import io.ebean.BaseTestCase;
import io.ebean.config.dbplatform.IdType;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.tests.model.basic.TOne;
import org.junit.Assert;
import org.junit.Test;

public class TestSeqBatch extends BaseTestCase {

  @Test
  public void test() {
    if (idType() != IdType.SEQUENCE) {
      return;
    }

    BeanDescriptor<TOne> d = spiEbeanServer().getBeanDescriptor(TOne.class);

    Object id = d.nextId(null);
    Assert.assertNotNull(id);
    // System.out.println(id);

    for (int i = 0; i < 16; i++) {
      Object id2 = d.nextId(null);
      Assert.assertNotNull(id2);
      // System.out.println(id2);
    }
  }

}
