package io.ebeaninternal.server.deploy;


import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.bean.EntityBean;
import org.junit.jupiter.api.Test;
import org.tests.model.mappedsuper.ASimpleBean;
import org.tests.model.mappedsuper.NotEnhancedMappedSuper;

import static org.junit.jupiter.api.Assertions.*;

public class TestNotEnhancedMappedSuper extends BaseTestCase {

  @Test
  public void simpleBean_mappedSuperNotEnhanced_ok() {

    ASimpleBean bean = new ASimpleBean();
    bean.setName("junk");

    DB.save(bean);
    assertNotNull(bean.getId());

    bean.setName("junk mod");
    DB.save(bean);

    ASimpleBean fetched = DB.find(ASimpleBean.class, bean.getId());
    assertEquals("junk mod", fetched.getName());
    assertEquals(bean.getName(), fetched.getName());

    DB.delete(bean);
    ASimpleBean fetched2 = DB.find(ASimpleBean.class, bean.getId());
    assertNull(fetched2);

    NotEnhancedMappedSuper mappedSuper = new NotEnhancedMappedSuper();
    assertTrue((mappedSuper instanceof EntityBean));
  }

}
