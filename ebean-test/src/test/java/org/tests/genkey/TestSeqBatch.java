package org.tests.genkey;

import io.ebean.BaseTestCase;
import io.ebean.config.dbplatform.IdType;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.TOne;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestSeqBatch extends BaseTestCase {

  @Test
  public void test() {
    if (idType() != IdType.SEQUENCE) {
      return;
    }

    BeanDescriptor<TOne> d = spiEbeanServer().descriptor(TOne.class);
    Object id = d.nextId(null);
    assertNotNull(id);
    for (int i = 0; i < 16; i++) {
      Object id2 = d.nextId(null);
      assertNotNull(id2);
    }
  }

}
