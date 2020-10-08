package org.tests.aggregateformula;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAggregateInheritFormula extends BaseTestCase {

  @Test
  public void test() {

    IAFSegmentStatus st0 = new IAFSegmentStatus("st0");
    IAFSegmentStatus st1 = new IAFSegmentStatus("st1");

    IAFPartialSegment p0 = new IAFPartialSegment(108, st0);
    IAFPartialSegment p1 = new IAFPartialSegment(108, st1);
    IAFPartialSegment p2 = new IAFPartialSegment(109, st0);

    DB.saveAll(Arrays.asList(st0, st1, p0, p1));

    LoggedSqlCollector.start();

    List<IAFPartialSegment> segments =
      DB.find(IAFPartialSegment.class)
        .select("segmentIdZat, min(status)")
        .where().eq("segmentIdZat", 108L)
        .findList();

    final List<String> sql = LoggedSqlCollector.stop();

    assertThat(segments).hasSize(1);
    assertSql(sql.get(0)).contains("select t0.segment_id_zat, min(t0.status_id) from iaf_segment t0 where t0.ptype = 'target' and t0.segment_id_zat = ? group by t0.segment_id_zat");
  }
}
