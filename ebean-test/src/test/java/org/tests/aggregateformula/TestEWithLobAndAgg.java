package org.tests.aggregateformula;

import io.ebean.DB;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestEWithLobAndAgg {

  @Test
  void when_lobAndAgg_expect_neitherLobOrAggSelectedByDefault() {
    var query = DB.find(EWithLobAndAgg.class);

    query.findList();
    var sql = query.getGeneratedSql();

    assertThat(sql)
      .describedAs("Neither Lob or Aggregation column in query")
      .isEqualTo("select t0.id, t0.name from ewith_lob_and_agg t0");
  }
}
