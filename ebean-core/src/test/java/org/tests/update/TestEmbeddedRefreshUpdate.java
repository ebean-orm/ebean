package org.tests.update;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.embedded.EEmbDatePeriod;
import org.tests.model.embedded.EEmbInner;
import org.tests.model.embedded.EEmbOuter;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class TestEmbeddedRefreshUpdate extends BaseTestCase {

  @Test
  public void test() {

    EEmbOuter outer = new EEmbOuter();
    outer.setNomeOuter("test");

    EEmbDatePeriod embeddedBean = new EEmbDatePeriod();
    embeddedBean.setDate1(new Date());

    outer.setDatePeriod(embeddedBean);

    DB.save(outer);

    EEmbOuter loaded = DB.find(EEmbOuter.class).where().idEq(outer.getId()).findOne();

    // if commented Ebean saves correctly
    DB.refresh(loaded);

    loaded.getDatePeriod().setDate2(new Date());

    // BUG 343
    DB.save(loaded);

    // See BUG 344
    DB.find(EEmbInner.class).fetch("outer").orderBy("outer.datePeriod.date1").findList();

  }

  @Test
  public void test2() {

    EEmbOuter outer = new EEmbOuter();
    outer.setNomeOuter("test");
    EEmbDatePeriod embeddedBean = new EEmbDatePeriod();
    embeddedBean.setDate1(new Date());
    outer.setDatePeriod(embeddedBean);

    DB.save(outer);
    assertThat(outer.getUpdateCount()).isEqualTo(1);

    Date d = new Date();
    d.setTime(1L);

    outer.getDatePeriod().setDate1(d);
    DB.save(outer);
    assertThat(outer.getUpdateCount()).isEqualTo(2);
  }
}
