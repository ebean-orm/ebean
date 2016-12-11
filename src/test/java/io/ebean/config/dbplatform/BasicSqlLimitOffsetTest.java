package io.ebean.config.dbplatform;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicSqlLimitOffsetTest {

  private BasicSqlLimitOffset limiter = new BasicSqlLimitOffset();

  @Test
  public void limit_maxRows() throws Exception {

    String query = "select * from mytab order by id";
    String sql = limiter.limit(query, 0, 10);
    assertThat(sql).isEqualTo(query + " limit 10");
  }

  @Test
  public void limit_firstRowMaxRows() throws Exception {

    String query = "select * from mytab order by id";
    String sql = limiter.limit(query, 5, 10);
    assertThat(sql).isEqualTo(query + " limit 10 offset 5");
  }

  @Test
  public void limit_zeros() throws Exception {

    String query = "select * from mytab order by id";
    String sql = limiter.limit(query, 0, 0);
    assertThat(sql).isEqualTo(query);
  }
}
