package org.tests.inheritance.cascadedelete;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNull;

public class TestInheritanceDeleteCascade extends BaseTestCase {

  @Test
  public void test() {

    RootBean bean1 = new ComplexBean(asList(new ElementBean("element-1"), new ElementBean("element-2")));
    RootBean bean2 = new SimpleBean("simple-1");
    ReferencingBean referencingBean = new ReferencingBean(asList(bean1, bean2));

    DB.save(referencingBean);
    DB.delete(referencingBean);

    assertNull(DB.find(RootBean.class, DB.getBeanId(bean1)));
    assertNull(DB.find(RootBean.class, DB.getBeanId(bean2)));
  }

}
