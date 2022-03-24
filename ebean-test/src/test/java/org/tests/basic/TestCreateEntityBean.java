package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EDefaultProp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestCreateEntityBean extends BaseTestCase {

  @IgnorePlatform(Platform.SQLSERVER)
  @Test
  public void testDefaultRelation() {

    // Use `new` to construct EntityBean
    final EDefaultProp beanNew = new EDefaultProp();
    DB.save(beanNew);

    final EDefaultProp beanNewDb = DB.find(EDefaultProp.class, beanNew.getId());
    assertNotNull(beanNewDb);
    assertEquals("defaultName", beanNew.getName());
    assertNotNull(beanNewDb.geteSimple());
    assertEquals("Default prop eSimple", beanNewDb.geteSimple().getName());

    // Use `createEntityBean` to construct EntityBean and check if it behaves the same as `new`
    final EDefaultProp beanCreateEntityBean = DB.getDefault().createEntityBean(EDefaultProp.class);
    DB.save(beanCreateEntityBean);

    final EDefaultProp beanCreateEntityBeanDb = DB.find(EDefaultProp.class, beanCreateEntityBean.getId());
    assertNotNull(beanCreateEntityBeanDb);
    assertEquals("defaultName", beanCreateEntityBeanDb.getName());
    assertNotNull(beanCreateEntityBeanDb.geteSimple());
    assertEquals("Default prop eSimple", beanCreateEntityBeanDb.geteSimple().getName());

  }

}
