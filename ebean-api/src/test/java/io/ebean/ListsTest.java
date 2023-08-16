package io.ebean;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListsTest {

  @Test
  void partition_empty() {
    List<List<Integer>> partitions = Lists.partition(5, List.of());
    assertThat(partitions).hasSize(1);
    assertThat(partitions.get(0)).hasSize(0);
  }

  @Test
  void partition_lt() {
    List<List<Integer>> partitions = Lists.partition(5, List.of(1, 2, 3, 4));
    assertThat(partitions).hasSize(1);
    assertThat(partitions.get(0)).hasSize(4);
  }

  @Test
  void partition_eq() {
    List<List<Integer>> partitions = Lists.partition(5, List.of(1, 2, 3, 4, 5));
    assertThat(partitions).hasSize(1);
    assertThat(partitions.get(0)).hasSize(5);
  }

  @Test
  void partition_gt() {
    List<List<Integer>> partitions = Lists.partition(5, List.of(1, 2, 3, 4, 5, 6));
    assertThat(partitions).hasSize(2);
    assertThat(partitions.get(0)).hasSize(5);
    assertThat(partitions.get(1)).hasSize(1);
    assertThat(partitions.get(0)).containsExactly(1, 2, 3, 4, 5);
    assertThat(partitions.get(1)).containsExactly(6);
  }

  @Test
  void partition_gt1_letters() {
    var partitions = Lists.partition(3, List.of("a", "b", "c", "d"));
    assertThat(partitions).hasSize(2);
    assertThat(partitions.get(0)).hasSize(3);
    assertThat(partitions.get(1)).hasSize(1);
    assertThat(partitions.get(0)).containsExactly("a", "b", "c");
    assertThat(partitions.get(1)).containsExactly("d");
  }

  @Test
  void partition_gt1a_letters() {
    var partitions = Lists.partition(3, List.of("a", "b", "c", "d", "e"));
    assertThat(partitions).hasSize(2);
    assertThat(partitions.get(0)).hasSize(3);
    assertThat(partitions.get(1)).hasSize(2);
    assertThat(partitions.get(0)).containsExactly("a", "b", "c");
    assertThat(partitions.get(1)).containsExactly("d", "e");
  }

  @Test
  void partition_eq2_letters() {
    var partitions = Lists.partition(3, List.of("a", "b", "c", "d", "e", "f"));
    assertThat(partitions).hasSize(2);
    assertThat(partitions.get(0)).hasSize(3);
    assertThat(partitions.get(1)).hasSize(3);
    assertThat(partitions.get(0)).containsExactly("a", "b", "c");
    assertThat(partitions.get(1)).containsExactly("d", "e", "f");
  }

  @Test
  void partition_gt2_letters() {
    var partitions = Lists.partition(3, List.of("a", "b", "c", "d", "e", "f", "g"));
    assertThat(partitions).hasSize(3);
    assertThat(partitions.get(0)).hasSize(3);
    assertThat(partitions.get(1)).hasSize(3);
    assertThat(partitions.get(2)).hasSize(1);
    assertThat(partitions.get(0)).containsExactly("a", "b", "c");
    assertThat(partitions.get(1)).containsExactly("d", "e", "f");
    assertThat(partitions.get(2)).containsExactly("g");
  }
}
