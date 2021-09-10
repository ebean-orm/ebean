package org.tests.query.embedded;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.ExpressionList;
import org.junit.jupiter.api.Test;
import org.tests.model.embedded.EEmbDatePeriod;
import org.tests.model.embedded.EEmbInner;
import org.tests.model.embedded.EEmbOuter;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test that selecting A and filtering on 'B.C=some_value' will correctly create join on table for B.
 * Model:
 * - Bean A has relation to bean B
 * - Bean B has an embedded property C
 */
public class TestFilteringByEmbeddedInJoinedTable extends BaseTestCase {

  private EEmbOuter createOuter(String nomeOuter, Date date1, Date date2) {
    EEmbOuter outer1 = new EEmbOuter();
    outer1.setNomeOuter(nomeOuter);

    EEmbDatePeriod eEmbDatePeriod = new EEmbDatePeriod();
    eEmbDatePeriod.setDate1(date1);
    eEmbDatePeriod.setDate2(date2);
    outer1.setDatePeriod(eEmbDatePeriod);

    DB.save(outer1);
    return outer1;
  }

  @Test
  public void testOuterTableJoined() {

    EEmbOuter outer1 = createOuter("outer1", new Date(11111), new Date(12222));
    // Unused outer2 just to populate the DB
    EEmbOuter outer2 = createOuter("outer2", new Date(21111), new Date(22222));
    assertThat(outer2).isNotNull();

    EEmbOuter outer3 = createOuter("outer3", new Date(31111), new Date(32222));


    EEmbInner inner1 = new EEmbInner();
    inner1.setOuter(outer1);
    inner1.setNomeInner("inner1-1");
    DB.save(inner1);

    EEmbInner inner2 = new EEmbInner();
    // Setting outer1 also here
    inner2.setOuter(outer1);
    inner2.setNomeInner("inner2-1");
    DB.save(inner2);

    EEmbInner inner3 = new EEmbInner();
    inner3.setOuter(outer3);
    inner3.setNomeInner("inner3-3");
    DB.save(inner3);

    ExpressionList<EEmbInner> el = DB.find(EEmbInner.class).where()
      .eq("outer.datePeriod.date1", new Date(11111));
    List<EEmbInner> list = el.findList();

    assertThat(list).hasSize(2);
  }
}




