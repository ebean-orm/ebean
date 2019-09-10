package org.tests.model.history;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.junit.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHistoryInclude extends BaseTestCase {

  private HiLink link;

  private void prepare() {
    if (link == null) {
      HiDoc docA = new HiDoc("doca");
      HiDoc docB = new HiDoc("docb");
      docA.save();
      docB.save();

      link = new HiLink("some", "link");
      link.getDocs().add(docA);
      link.getDocs().add(docB);
      link.save();
    }
  }

  @Test
  public void testLazyLoad() {

    prepare();

    HiLink linkFound = Ebean.find(HiLink.class, link.getId());
    assertThat(linkFound.getDocs().size()).isEqualTo(2);
  }

  @IgnorePlatform(Platform.ORACLE)
  @Test
  public void testAsOfThenLazy() {

    prepare();

    HiLink linkFound = Ebean.find(HiLink.class)
      .asOf(new Timestamp(System.currentTimeMillis() + DB_CLOCK_DELTA))
      .setId(link.getId())
      .findOne();

    assertThat(linkFound.getDocs().size()).isEqualTo(2);
  }
}
