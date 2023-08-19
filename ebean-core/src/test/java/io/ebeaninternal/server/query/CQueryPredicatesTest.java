package io.ebeaninternal.server.query;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CQueryPredicatesTest {

  @Test
  void filterManyPaths_one() {
    String result = CQueryPredicates.filterManyPaths("contacts", "${}first is not null");
    assertThat(result).isEqualTo("${contacts}first is not null");
  }

  @Test
  void filterManyPaths_nested() {
    String result = CQueryPredicates.filterManyPaths("contacts", "${address}city");
    assertThat(result).isEqualTo("${contacts.address}city");
  }

  @Test
  void filterManyPaths_2() {
    String result = CQueryPredicates.filterManyPaths("contacts", "${}first ${}last");
    assertThat(result).isEqualTo("${contacts}first ${contacts}last");
  }
}
