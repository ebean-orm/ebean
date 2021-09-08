package org.tests.contructor;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.bean.EntityBean;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.MProtectedConstructBean;

import static org.junit.jupiter.api.Assertions.*;

public class TestProtectedConstructor extends BaseTestCase {

  @Test
  public void test() {

    Database server = DB.getDefault();

    // check that we can construc a bean with a protected constructor
    MProtectedConstructBean bean = server.createEntityBean(MProtectedConstructBean.class);
    assertNotNull(bean);

    // Note1 that the enhancement ClassAdapterEntity line 239 makes a default constructor publically accessible
    // Note2 the ClassAdpater will call DefaultConstructor.add() to add a default constructor if it doesn't exist


    EntityBean entityBean = (EntityBean) bean;
    Object newBeanInstance = entityBean._ebean_newInstance();

    assertNotNull(newBeanInstance);
    BeanState beanState = DB.beanState(newBeanInstance);
    assertTrue(beanState.isNew());
    assertNotSame(entityBean, newBeanInstance);

  }

}
