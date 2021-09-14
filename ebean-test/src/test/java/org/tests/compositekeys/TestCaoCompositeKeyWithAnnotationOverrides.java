package org.tests.compositekeys;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.compositekeys.db.CaoBean;
import org.tests.compositekeys.db.CaoKey;

public class TestCaoCompositeKeyWithAnnotationOverrides extends BaseTestCase {

  @Test
  public void test() {

    DB.deleteAll(DB.find(CaoBean.class).findList());

    CaoKey key = new CaoKey();
    key.setCustomer(123);
    key.setType(1);

    CaoBean bean = new CaoBean();
    bean.setKey(key);
    bean.setDescription("some desc");

    DB.save(bean);
  }

}
