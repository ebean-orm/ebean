package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.basic.EDefaultProp;

public class TestCreateEntityBean extends BaseTestCase {

  @Test
  public void testDefaultRelation() {
    // Use `new` to construct EntityBean
    final EDefaultProp beanNew = new EDefaultProp();
    Ebean.save(beanNew);

    final EDefaultProp beanNewDb = Ebean.find(EDefaultProp.class, beanNew.getId());
    Assert.assertNotNull(beanNewDb);
    Assert.assertEquals("defaultName", beanNew.getName());
    Assert.assertNotNull(beanNewDb.geteSimple());
    Assert.assertEquals("Default prop eSimple", beanNewDb.geteSimple().getName());

    // Use `createEntityBean` to construct EntityBean and check if it behaves the same as `new`
    final EDefaultProp beanCreateEntityBean = DB.getDefault().createEntityBean(EDefaultProp.class);
    Ebean.save(beanCreateEntityBean);

    final EDefaultProp beanCreateEntityBeanDb = Ebean.find(EDefaultProp.class, beanCreateEntityBean.getId());
    Assert.assertNotNull(beanCreateEntityBeanDb);
    Assert.assertEquals("defaultName", beanCreateEntityBeanDb.getName());
    Assert.assertNotNull(beanCreateEntityBeanDb.geteSimple());
    Assert.assertEquals("Default prop eSimple", beanCreateEntityBeanDb.geteSimple().getName());

  }

}
