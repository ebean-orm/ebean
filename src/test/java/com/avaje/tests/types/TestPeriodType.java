package com.avaje.tests.types;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.types.SomePeriodBean;
import org.junit.Test;

import java.time.Period;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 */
public class TestPeriodType extends BaseTestCase {

  @Test
  public void testInsert() {

    SomePeriodBean bean = new SomePeriodBean();
    bean.setPeriod(Period.of(3, 4, 5));
    Ebean.save(bean);

    SomePeriodBean bean1 = Ebean.find(SomePeriodBean.class, bean.getId());
    assertEquals(bean.getPeriod(), bean1.getPeriod());

    // insert fetch null value
    SomePeriodBean bean2 = new SomePeriodBean();
    Ebean.save(bean2);

    SomePeriodBean bean3 = Ebean.find(SomePeriodBean.class, bean2.getId());
    assertNull(bean3.getPeriod());
  }

}
