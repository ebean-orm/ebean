package com.avaje.tests.compositekeys;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.compositekeys.db.CaoBean;
import com.avaje.tests.compositekeys.db.CaoKey;
import org.junit.Test;

public class TestCaoCompositeKeyWithAnnotationOverrides extends BaseTestCase {

  @Test
  public void test() {

    Ebean.deleteAll(Ebean.find(CaoBean.class).findList());

    CaoKey key = new CaoKey();
    key.setCustomer(123);
    key.setType(1);

    CaoBean bean = new CaoBean();
    bean.setKey(key);
    bean.setDescription("some desc");

    Ebean.save(bean);
  }

}
