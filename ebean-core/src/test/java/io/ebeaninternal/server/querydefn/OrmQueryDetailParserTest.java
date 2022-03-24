package io.ebeaninternal.server.querydefn;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


public class OrmQueryDetailParserTest {

  OrmQueryDetail parse(String query) {
    return new OrmQueryDetailParser(query).parse();
  }

  @Test
  public void parse_when_nullString() {
    assertThrows(NullPointerException.class, () -> parse(null));
  }

  @Test
  public void parse_when_emptyString() {
    assertTrue(parse("").isEmpty());
  }

  @Test
  public void testParseBasic() {
    OrmQueryDetail detail = parse("select (id,name)");

    OrmQueryProperties root = detail.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getIncluded()).containsExactly("id", "name");
  }

  @Test
  public void testParseEmptySelect() {
    OrmQueryDetail detail = parse("select  fetch customer (email)");

    OrmQueryProperties root = detail.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getIncluded()).isNull();

    OrmQueryProperties chunk = detail.getChunk("customer", false);
    assertThat(chunk.getPath()).isEqualTo("customer");
    assertThat(chunk.getIncluded()).contains("email");
  }

  @Test
  public void testParseSelectFetch() {
    OrmQueryDetail detail = parse("select (id,name) fetch customer (email)");

    OrmQueryProperties root = detail.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getIncluded()).contains("id", "name");

    OrmQueryProperties chunk = detail.getChunk("customer", false);
    assertThat(chunk.getPath()).isEqualTo("customer");
    assertThat(chunk.getIncluded()).contains("email");
  }

  @Test
  public void testParseSelectFetchMore() {
    OrmQueryDetail detail = parse("select (id,name) fetch customer (email) fetch details.product (sku,description)");

    OrmQueryProperties root = detail.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getIncluded()).contains("id", "name");

    OrmQueryProperties chunk = detail.getChunk("customer", false);
    assertThat(chunk.getPath()).isEqualTo("customer");
    assertThat(chunk.getIncluded()).contains("email");

    chunk = detail.getChunk("details.product", false);
    assertThat(chunk.getPath()).isEqualTo("details.product");
    assertThat(chunk.getIncluded()).contains("sku", "description");
  }

  @Test
  public void testParseWithPlusQuery() {
    OrmQueryDetail detail = parse("select (id,name) fetch customer (+query,id,name,email)");

    OrmQueryProperties root = detail.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getIncluded()).contains("id", "name");

    OrmQueryProperties chunk = detail.getChunk("customer", false);
    assertThat(chunk.getPath()).isEqualTo("customer");
    assertThat(chunk.getIncluded()).contains("id", "name", "email");
    //FIXME: assertThat(chunk.isQueryFetch()).isTrue();
  }

  @Test
  public void testTuneApply() {

    OrmQueryDetail detail = parse("select (status) fetch customer (email)");
    OrmQueryDetail tune = parse("select (id,name) fetch customer (+query,id,name,email)");

    detail.tuneFetchProperties(tune);

    OrmQueryProperties root = detail.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getIncluded()).contains("id", "name");

    OrmQueryProperties chunk = detail.getChunk("customer", false);
    assertThat(chunk.getPath()).isEqualTo("customer");
    assertThat(chunk.getIncluded()).contains("id", "name", "email");
    //FIXME: assertThat(chunk.isQueryFetch()).isTrue();
  }

}
