package org.tests.model.history;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHistoryExclude extends BaseTestCase {

  private HeLink link;

  private void prepare() {
    if (link == null) {
      HeDoc docA = new HeDoc("doca");
      HeDoc docB = new HeDoc("docb");
      docA.save();
      docB.save();

      link = new HeLink("some", "link");
      link.getDocs().add(docA);
      link.getDocs().add(docB);
      link.save();
    }
  }

  @Test
  public void testSoftDelete_includeSoftDeletes_findList() {

    HeLink l = new HeLink("two", "boo");
    Ebean.save(l);

    Ebean.delete(l);

    List<HeLink> list = Ebean.find(HeLink.class)
      .setIncludeSoftDeletes()
      .findList();

    assertThat(list).isNotEmpty();
  }

  @Test
  public void testSoftDelete_includeSoftDeletes_findOne() {

    HeLink l = new HeLink("three", "boo2");
    Ebean.save(l);

    Ebean.delete(l);

    HeLink found = Ebean.find(HeLink.class)
      .setId(l.getId())
      .setIncludeSoftDeletes()
      .findOne();

    assertThat(found).isNotNull();
    assertThat(found.getName()).isEqualTo("three");
  }

  @Test
  public void testLazyLoad() {

    prepare();

    HeLink linkFound = Ebean.find(HeLink.class, link.getId());
    linkFound.getDocs().size();
  }

  @IgnorePlatform(Platform.ORACLE)
  @Test
  public void testAsOfThenLazy() {

    prepare();

    HeLink linkFound = Ebean.find(HeLink.class)
      .asOf(new Timestamp(System.currentTimeMillis() + DB_CLOCK_DELTA))
      .setId(link.getId())
      .findOne();

    assertThat(linkFound.getDocs().size()).isEqualTo(2);
  }
}
