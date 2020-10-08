package io.ebeaninternal.server.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class BindPaddingTest {

  @Test
  public void padIds() {

    final List<Object> input = asList(1, 2);
    BindPadding.padIds(input);
    assertThat(input).contains(1,2,1,1,1);
    assertThat(input).hasSize(5);
  }

  private List<Object> asList(int... id) {
    List<Object> list = new ArrayList<>();
    for (int i : id) {
      list.add(i);
    }
    return list;
  }

  @Test
  public void padding() {

    assertEquals(0, BindPadding.padding(1));
    assertEquals(3, BindPadding.padding(2));
    assertEquals(2, BindPadding.padding(3));
    assertEquals(1, BindPadding.padding(4));
    assertEquals(0, BindPadding.padding(5));
    assertEquals(4, BindPadding.padding(6));
    assertEquals(1, BindPadding.padding(9));
    assertEquals(0, BindPadding.padding(10));
    assertEquals(9, BindPadding.padding(11));
    assertEquals(0, BindPadding.padding(20));
    assertEquals(19, BindPadding.padding(21));
    assertEquals(0, BindPadding.padding(40));
    assertEquals(9, BindPadding.padding(41));
    assertEquals(0, BindPadding.padding(50));
    assertEquals(49, BindPadding.padding(51));
    assertEquals(0, BindPadding.padding(100));
    assertEquals(0, BindPadding.padding(101));
  }
}
