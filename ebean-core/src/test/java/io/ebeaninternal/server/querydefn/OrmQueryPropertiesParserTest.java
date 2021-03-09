package io.ebeaninternal.server.querydefn;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrmQueryPropertiesParserTest {

  @Test
  public void when_null() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse(null);
    assertAllDefaults(res);
    assertThat(res.allProperties).isFalse();
    assertThat(res.included).isNull();
  }

  @Test
  public void when_empty() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("");
    assertAllDefaults(res);
    assertThat(res.allProperties).isFalse();
    assertThat(res.included).isNull();
  }

  @Test
  public void when_hasStar() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("*");
    assertAllDefaults(res);
    assertThat(res.allProperties).isTrue();
    assertThat(res.included).isNull();
  }

  @Test
  public void when_no_spaces() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("id,name");
    assertThat(res.allProperties).isFalse();
    assertThat(res.included).containsExactly("id", "name");
  }

  @Test
  public void when_spaced() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("id, name");
    assertThat(res.allProperties).isFalse();
    assertThat(res.included).containsExactly("id", "name");
  }

  @Test
  public void when_formula() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("a,MD5(id::text) as b,c");
    assertThat(res.allProperties).isFalse();
    assertThat(res.included).containsExactly("a", "MD5(id::text) as b", "c");
  }

  private void assertAllDefaults(OrmQueryPropertiesParser.Response res) {
    assertThat(res.included).isNull();
  }
}
