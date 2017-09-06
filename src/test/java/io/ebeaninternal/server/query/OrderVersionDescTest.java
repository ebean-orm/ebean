package io.ebeaninternal.server.query;

import io.ebean.Version;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class OrderVersionDescTest {

  private final long now = System.currentTimeMillis();

  @Test
  public void sort() {

    Version<?> atNull = atNull();
    Version<?> at100 = at(100);
    Version<?> at200 = at(200);
    Version<?> at300 = at(300);

    List<Version<?>> versions = new ArrayList<>();
    versions.add(at200);
    versions.add(atNull);
    versions.add(at300);
    versions.add(at100);

    Collections.sort(versions, OrderVersionDesc.INSTANCE);

    assertThat(versions.get(0)).isSameAs(at300);
    assertThat(versions.get(1)).isSameAs(at200);
    assertThat(versions.get(2)).isSameAs(at100);
    assertThat(versions.get(3)).isSameAs(atNull);
  }

  @Test
  public void compare_lt() {

    assertEquals(OrderVersionDesc.INSTANCE.compare(at(0), at(1)), 1);
  }

  @Test
  public void compare_gt() {

    assertEquals(OrderVersionDesc.INSTANCE.compare(at(2), at(1)), -1);
  }

  @Test
  public void compare_eq() {

    assertEquals(OrderVersionDesc.INSTANCE.compare(at(1), at(1)), 0);
  }

  @Test
  public void compare_nullFirst() {

    assertEquals(OrderVersionDesc.INSTANCE.compare(atNull(), at(1)), 1);
  }


  @Test
  public void compare_nullLast() {

    assertEquals(OrderVersionDesc.INSTANCE.compare(at(0), atNull()), -1);
  }

  private Version<?> atNull() {
    return new Version<>();
  }

  private Version<?> at(long diff) {
    Timestamp timestamp = new Timestamp(now + diff);
    Version<?> ver = new Version<>();
    ver.setStart(timestamp);
    return ver;
  }
}
