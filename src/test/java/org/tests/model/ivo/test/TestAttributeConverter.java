package org.tests.model.ivo.test;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.ivo.ESomeConvertType;
import org.tests.model.ivo.Money;
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
