package io.ebeaninternal.server.querydefn;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrmQueryPropertiesParserTest {

  @Test
  public void when_null() {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse(null);
    assertAllDefaults(res);
    assertThat(res.properties).isEqualTo("");
  }

  @Test
  public void when_empty() throws Exception {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("");
    assertAllDefaults(res);
    assertThat(res.properties).isEqualTo("");
  }

  @Test
  public void when_hasStar() throws Exception {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("*");
    assertAllDefaults(res);
    assertThat(res.properties).isEqualTo("*");
  }

  @Test
  public void when_hasCache() throws Exception {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("+cache");
    assertThat(res.cache).isTrue();
    assertThat(res.included).isNull();
  }

  @Test
  public void when_hasCache_first() throws Exception {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("+cache,id");
    assertThat(res.cache).isTrue();
    assertThat(res.included).containsExactly("id");
  }

  @Test
  public void when_hasCache_last() throws Exception {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("name,+cache");
    assertThat(res.cache).isTrue();
    assertThat(res.included).containsExactly("name");
  }

  @Test
  public void when_hasCache_middle() throws Exception {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("name,+cache, id");
    assertThat(res.cache).isTrue();
    assertThat(res.included).containsExactly("name", "id");
  }

  @Test
  public void when_hasReadOnly() throws Exception {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("+readonly");
    assertThat(res.readOnly).isTrue();
    assertThat(res.included).isNull();
  }

  @Test
  public void when_hasLazy() throws Exception {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("+lazy");
    assertThat(res.fetchConfig.getLazyBatchSize()).isEqualTo(0);
    assertThat(res.included).isNull();
  }

  @Test
  public void when_hasLazyValue() throws Exception {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("+lazy(20)");
    assertThat(res.fetchConfig.getLazyBatchSize()).isEqualTo(20);
    assertThat(res.included).isNull();
  }

  @Test
  public void when_hasLazyValue_last() throws Exception {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("name,+lazy(20)");
    assertThat(res.fetchConfig.getLazyBatchSize()).isEqualTo(20);
    assertThat(res.included).containsExactly("name");
  }

  @Test
  public void when_hasLazyValue_first() throws Exception {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("+lazy(20),id,name");
    assertThat(res.fetchConfig.getLazyBatchSize()).isEqualTo(20);
    assertThat(res.included).containsExactly("id", "name");
  }

  @Test
  public void when_allProperties() throws Exception {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("+query(4),+lazy(5)");
    assertThat(res.fetchConfig.getLazyBatchSize()).isEqualTo(5);
    assertThat(res.included).isNull();
  }

  @Test
  public void when_everything_set() throws Exception {

    OrmQueryPropertiesParser.Response res = OrmQueryPropertiesParser.parse("id, name +readonly +lazy(20) +query(30) +cache");
    assertThat(res.fetchConfig.getLazyBatchSize()).isEqualTo(20);
    assertThat(res.fetchConfig.getQueryBatchSize()).isEqualTo(30);
    assertThat(res.readOnly).isTrue();
    assertThat(res.cache).isTrue();
    assertThat(res.included).containsExactly("id", "name");
  }

  private void assertAllDefaults(OrmQueryPropertiesParser.Response res) {
    assertThat(res.cache).isFalse();
    assertThat(res.readOnly).isFalse();
    assertThat(res.fetchConfig.getLazyBatchSize()).isEqualTo(-1);
    assertThat(res.fetchConfig.getQueryBatchSize()).isEqualTo(-1);
    assertThat(res.included).isNull();
  }
}
