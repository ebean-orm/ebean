package com.avaje.tests.model.ivo.test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.ivo.ESomeConvertType;
import com.avaje.tests.model.ivo.Money;
import org.junit.Test;

public class TestAttributeConverter extends BaseTestCase {

  @Test
  public void insertUpdateDelete() {

    ESomeConvertType bean = new ESomeConvertType("one", new Money(20.1));

    Ebean.save(bean);

    ESomeConvertType found = Ebean.find(ESomeConvertType.class, bean.getId());
    found.setMoney(new Money("40"));
    Ebean.save(found);

    Ebean.delete(found);
  }
}
