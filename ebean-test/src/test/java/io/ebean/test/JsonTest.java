package io.ebean.test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.test.PlainBean;

import java.util.List;

import static io.ebean.test.Json.readNodeFromResource;
import static io.ebean.test.Json.readResource;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonTest {

  @Test
  public void assertContains_subset() {
    JsonNode original = readNodeFromResource("/contains/original.json");
    JsonNode expected = readNodeFromResource("/contains/original-subset.json");
    Json.assertContains(original, expected);
  }

  @Test
  public void readNode() {
    JsonNode original = Json.readNode(readResource("/contains/original.json"));
    Json.assertContains(original, original);
  }

  @Test
  public void resource_asNode() {
    JsonNode original = Json.resource("/contains/original.json").asNode();
    JsonNode nonFluid = Json.readNode(readResource("/contains/original.json"));
    assertThat(original).isEqualTo(nonFluid);
  }

  @Test
  public void readBean_resource_asBean() {
    // traditional style from resource
    PlainBean bean1 = Json.readFromResource(PlainBean.class, "/example/plain.json");
    assertThat(bean1.id).isEqualTo(42);
    assertThat(bean1.name).isEqualTo("foo");

    // traditional style given json content
    PlainBean bean2 = Json.read(PlainBean.class, readResource("/example/plain.json"));
    assertThat(bean2.id).isEqualTo(42);
    assertThat(bean2.name).isEqualTo("foo");

    // fluid style - resource asBean()
    PlainBean bean3 = Json.resource("/example/plain.json").asBean(PlainBean.class);
    assertThat(bean3.id).isEqualTo(42);
    assertThat(bean3.name).isEqualTo("foo");
  }

  @Test
  public void readList() {
    List<PlainBean> list = Json.readList(PlainBean.class, readResource("/example/plain-list.json"));

    String asJson = Json.toJsonString(list);
    assertThat(list).hasSize(2);
    assertThat(list.get(0).name).isEqualTo("foo");
    assertThat(list.get(1).name).isEqualTo("bar");

    assertThat(asJson).contains("\"name\" : \"foo\"");
  }

  @Test
  public void resourceAsList() {
    // fluid style - resource asList()
    List<PlainBean> list = Json.resource("/example/plain-list.json").asList(PlainBean.class);

    // traditional style
    List<PlainBean> list2 = Json.readList(PlainBean.class, readResource("/example/plain-list.json"));
    assertThat(list).isEqualTo(list2);
  }

}
