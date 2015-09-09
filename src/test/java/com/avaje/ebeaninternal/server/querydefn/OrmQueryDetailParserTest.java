package com.avaje.ebeaninternal.server.querydefn;

import com.avaje.ebean.BaseTestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;


public class OrmQueryDetailParserTest extends BaseTestCase {

  @Test
  public void testParseBasic() throws Exception {


    OrmQueryDetail other = new OrmQueryDetail();
    other.select("id,name");

    OrmQueryProperties root = other.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getAllIncludedProperties()).contains("id", "name");

    OrmQueryDetailParser p = new OrmQueryDetailParser("select (id,name)");
    OrmQueryDetail detail = p.parse();

    root = detail.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getAllIncludedProperties()).contains("id", "name");
  }

  @Test
  public void testParseEmptySelect() throws Exception {

    OrmQueryDetailParser p = new OrmQueryDetailParser("select  fetch customer (email)");
    OrmQueryDetail detail = p.parse();

    OrmQueryProperties root = detail.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getAllIncludedProperties()).isNull();

    OrmQueryProperties chunk = detail.getChunk("customer", false);
    assertThat(chunk.getPath()).isEqualTo("customer");
    assertThat(chunk.getAllIncludedProperties()).contains("email");
  }

  @Test
  public void testParseSelectFetch() throws Exception {

    OrmQueryDetailParser p = new OrmQueryDetailParser("select (id,name) fetch customer (email)");
    OrmQueryDetail detail = p.parse();

    OrmQueryProperties root = detail.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getAllIncludedProperties()).contains("id", "name");

    OrmQueryProperties chunk = detail.getChunk("customer", false);
    assertThat(chunk.getPath()).isEqualTo("customer");
    assertThat(chunk.getAllIncludedProperties()).contains("email");
  }

  @Test
  public void testParseSelectFetchMore() throws Exception {

    OrmQueryDetailParser p = new OrmQueryDetailParser("select (id,name) fetch customer (email) fetch details.product (sku,description)");
    OrmQueryDetail detail = p.parse();

    OrmQueryProperties root = detail.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getAllIncludedProperties()).contains("id", "name");

    OrmQueryProperties chunk = detail.getChunk("customer", false);
    assertThat(chunk.getPath()).isEqualTo("customer");
    assertThat(chunk.getAllIncludedProperties()).contains("email");

    chunk = detail.getChunk("details.product", false);
    assertThat(chunk.getPath()).isEqualTo("details.product");
    assertThat(chunk.getAllIncludedProperties()).contains("sku","description");
  }

  @Test
  public void testParseWithPlusQuery() throws Exception {

    OrmQueryDetailParser p = new OrmQueryDetailParser("select (id,name) fetch customer (+query,id,name,email)");
    OrmQueryDetail detail = p.parse();

    OrmQueryProperties root = detail.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getAllIncludedProperties()).contains("id", "name");

    OrmQueryProperties chunk = detail.getChunk("customer", false);
    assertThat(chunk.getPath()).isEqualTo("customer");
    assertThat(chunk.getAllIncludedProperties()).contains("id", "name", "email");
    assertThat(chunk.isQueryFetch()).isTrue();

  }

  @Test
  public void testTuneApply() {

    OrmQueryDetailParser p = new OrmQueryDetailParser("select (status) fetch customer (email)");
    OrmQueryDetail detail = p.parse();

    OrmQueryDetailParser p2 = new OrmQueryDetailParser("select (id,name) fetch customer (+query,id,name,email)");
    OrmQueryDetail tune = p2.parse();


    detail.tuneFetchProperties(tune);

    OrmQueryProperties root = detail.getChunk(null, false);
    assertNull(root.getPath());
    assertThat(root.getAllIncludedProperties()).contains("id", "name");

    OrmQueryProperties chunk = detail.getChunk("customer", false);
    assertThat(chunk.getPath()).isEqualTo("customer");
    assertThat(chunk.getAllIncludedProperties()).contains("id", "name", "email");
    assertThat(chunk.isQueryFetch()).isTrue();
  }

}