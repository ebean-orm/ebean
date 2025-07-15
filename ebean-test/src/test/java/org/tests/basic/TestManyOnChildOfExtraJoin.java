package org.tests.basic;

import io.ebean.DB;
import io.ebean.Query;
import io.ebean.xtest.BaseTestCase;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Country;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.OBeanChild;
import org.tests.model.basic.OCachedBean;
import org.tests.model.basic.ResetBasicData;

public class TestManyOnChildOfExtraJoin extends BaseTestCase {


  @Test
  public void test() {
    ResetBasicData.reset();

    OCachedBean bean = new OCachedBean();
    bean.setName("m2m-with-sq");
    bean.getCountries().add(DB.reference(Country.class, "NZ"));
    bean.getCountries().add(DB.reference(Country.class, "AU"));
    DB.save(bean);

    OBeanChild child = new OBeanChild();
    child.setCachedBean(bean);
    DB.save(child);

    EBasic b1 = new EBasic();
    b1.setName("Australia");
    b1.setStatus(EBasic.Status.ACTIVE);
    DB.save(b1);

    EBasic b2 = new EBasic();
    b2.setName("New Zealand");
    b2.setStatus(EBasic.Status.ACTIVE);
    DB.save(b2);

    Query<OBeanChild> query = DB.find(OBeanChild.class).where()
      .eq("cachedBean.name", "m2m-with-sq")
      .exists(DB.find(EBasic.class)
        .alias("sq1")
        .where()
        .raw("cachedBean.countries.name = sq1.name")
        .eq("status", EBasic.Status.ACTIVE)
        .query())
      .query();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(query.findList()).hasSize(1);
    softly.assertThat(query.getGeneratedSql()).startsWith("select distinct");
    softly.assertAll();
  }
}
