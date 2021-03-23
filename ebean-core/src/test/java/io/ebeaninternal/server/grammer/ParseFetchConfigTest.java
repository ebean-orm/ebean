package io.ebeaninternal.server.grammer;

import io.ebean.FetchConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

public class ParseFetchConfigTest {

  @Test
  public void parse() throws Exception {

    assertNull(ParseFetchConfig.parse("junk"));
    assertNull(ParseFetchConfig.parse("lazyFoo"));
    assertNull(ParseFetchConfig.parse("queryFoo"));
  }

  @Test
  public void parseLazy() {
    FetchConfig lazy = ParseFetchConfig.parse("lazy");
    assertThat(lazy.getBatchSize()).isEqualTo(0);
  }

  @Test
  public void parseLazy100() {
    FetchConfig lazy = ParseFetchConfig.parse("lazy(100)");
    assertThat(lazy.getBatchSize()).isEqualTo(100);
  }

  @Test
  public void parseQuery() {
    FetchConfig lazy = ParseFetchConfig.parse("query");
    assertThat(lazy.getBatchSize()).isEqualTo(100);
  }

  @Test
  public void parseQuery100() {
    FetchConfig lazy = ParseFetchConfig.parse("query(50)");
    assertThat(lazy.getBatchSize()).isEqualTo(50);
  }
}
