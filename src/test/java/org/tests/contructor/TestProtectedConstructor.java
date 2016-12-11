package org.tests.contructor;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.bean.EntityBean;
import org.tests.model.basic.MProtectedConstructBean;
import org.junit.Assert;
import org.junit.Test;

public class TestProtectedConstructor extends BaseTestCase {

  @Test
  public void test() {

    EbeanServer server = Ebean.getServer(null);

    // check that we can construc a bean with a protected constructor
    MProtectedConstructBean bean = server.createEntityBean(MProtectedConstructBean.class);
    Assert.assertNotNull(bean);

    // Note1 that the enhancement ClassAdapterEntity line 239 makes a default constructor publically accessible
    // Note2 the ClassAdpater will call DefaultConstructor.add() to add a default constructor if it doesn't exist


    EntityBean entityBean = (EntityBean) bean;
    Object newBeanInstance = entityBean._ebean_newInstance();

    Assert.assertNotNull(newBeanInstance);
    BeanState beanState = Ebean.getBeanState(newBeanInstance);
    Assert.assertTrue(beanState.isNew());
    Assert.assertNotSame(entityBean, newBeanInstance);

  }

}
