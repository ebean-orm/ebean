package org.tests.o2m;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestOneToManyOnlyBean extends BaseTestCase {

  @Test
  public void insert() {

    OMVertex myBean = new OMVertex(UUID.randomUUID());
    DB.save(myBean);

    assertEquals(DB.find(OMVertex.class, myBean.getId()), myBean);
  }

  @Test
  public void isReference_when_newWithId_expectFalse() {

    OMVertex myBean = new OMVertex(UUID.randomUUID());
    assertFalse(isReference(myBean));
  }

  @Test
  public void isReference_when_getReference_expectTrue() {

    OMVertex myBean = DB.getReference(OMVertex.class, UUID.randomUUID());
    assertTrue(isReference(myBean));
  }

  private boolean isReference(Object myBean) {
    final BeanDescriptor<OMVertex> desc = getBeanDescriptor(OMVertex.class);
    return desc.isReference(((EntityBean) myBean)._ebean_getIntercept());
  }
}
