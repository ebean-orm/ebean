package io.ebean.config.dbplatform;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicSqlStandardLimiterTest {

  private BasicSqlAnsiLimiter limiter = new BasicSqlAnsiLimiter();

  @Test
  public void limit_maxRows() throws Exception {

    String query = "select * from mytab order by id";
    String sql = limiter.limit(query, 0, 10);
    assertThat(sql).isEqualTo(query + " fetch next 10 rows only");
  }

  @Test
  public void limit_firstRowMaxRows() throws Exception {

    String query = "select * from mytab order by id";
    String sql = limiter.limit(query, 5, 10);
    assertThat(sql).isEqualTo(query + " offset 5 rows fetch next 10 rows only");
  }

  @Test
  public void limit_zeros() throws Exception {

    String query = "select * from mytab order by id";
    String sql = limiter.limit(query, 0, 0);
    assertThat(sql).isEqualTo(query);
  }

}
