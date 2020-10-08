package io.ebeaninternal.server.profile;

import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.SortMetric;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class SortMetricTest {

  private Comparator<MetaTimedMetric> sortMetric = SortMetric.NAME;

  @Test
  public void compare_list() {

    List<DTimeMetricStats> list = new ArrayList<>();
    list.add(create("d"));
    list.add(create("b"));
    list.add(create("c"));
    list.add(create(null));
    list.add(create("a"));
    list.sort(sortMetric);

    String names = list.stream().map(DTimeMetricStats::getName).collect(Collectors.joining());

    assertEquals("nullabcd", names);
  }

  @Test
  public void compare_when_same() {

    assertEquals(0, sortMetric.compare(create("foo"), create("foo")));
    assertEquals(0, sortMetric.compare(create(null), create(null)));
  }

  @Test
  public void compare_when_less() {

    assertEquals(-1, sortMetric.compare(create("a"), create("b")));
    assertEquals(-1, sortMetric.compare(create("foo"), create("goo")));
  }

  @Test
  public void compare_when_more() {

    assertEquals(1, sortMetric.compare(create("b"), create("a")));
    assertEquals(1, sortMetric.compare(create("goo"), create("foo")));
  }

  @Test
  public void compare_when_nulls() {

    assertEquals(0, sortMetric.compare(create(null), create(null)));
    assertEquals(1, sortMetric.compare(create("foo"), create(null)));
    assertEquals(-1, sortMetric.compare(create(null), create("foo")));
  }

  private DTimeMetricStats create(String name) {
    return new DTimeMetricStats(name, false, 0, 0, 0);
  }
}
