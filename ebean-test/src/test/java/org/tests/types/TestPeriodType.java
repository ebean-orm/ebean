package org.tests.types;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.types.SomePeriodBean;

import java.sql.Date;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestPeriodType extends BaseTestCase {

  @Test
  public void testInsert() {

    SomePeriodBean bean = new SomePeriodBean();
    bean.setAnniversary(MonthDay.of(4, 29));
    DB.save(bean);

    SomePeriodBean bean1 = DB.find(SomePeriodBean.class, bean.getId());
    assertEquals(bean.getAnniversary(), bean1.getAnniversary());

    // insert fetch null value
    SomePeriodBean bean2 = new SomePeriodBean();
    DB.save(bean2);

    SomePeriodBean bean3 = DB.find(SomePeriodBean.class, bean2.getId());
    assertNull(bean3.getAnniversary());

    List<SomePeriodBean> anniversaryList = DB.find(SomePeriodBean.class)
      .where()
      .eq("anniversary", MonthDay.of(4, 29))
      .findList();

    assertEquals(1, anniversaryList.size());

    // must use year 2000 for range predicates
    // ... using 2001 here so not finding anything
    anniversaryList = DB.find(SomePeriodBean.class)
      .where()
      .gt("anniversary", Date.valueOf(LocalDate.of(2001, 4, 29)))
      .findList();

    assertEquals(0, anniversaryList.size());

    // can use year 2000 for range predicates
    // ... and can use LocalDate to bind
    anniversaryList = DB.find(SomePeriodBean.class)
      .where()
      .gt("anniversary", MonthDay.of(4, 22))
      .findList();

    assertEquals(1, anniversaryList.size());

    DB.delete(bean);
    DB.delete(bean2);
  }

}
