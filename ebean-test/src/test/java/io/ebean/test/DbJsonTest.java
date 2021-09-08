package io.ebean.test;

import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.DB;
import org.etest.BSimpleFor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.ebean.test.DbJson.readResource;
import static org.assertj.core.api.Assertions.assertThat;


public class DbJsonTest {

  @Test
  public void of() {

    DB.find(BSimpleFor.class).delete();

    BSimpleFor bean = new BSimpleFor("something");
    DB.save(bean);

    assertThat(bean.getVersion()).isEqualTo(1);

    DbJson.of(bean)
      .withPlaceholder("_")
      .replace("id", "whenModified")
      .assertContentMatches("/bean/example-bean.json");


    BSimpleFor bean2 = new BSimpleFor("other");
    DB.save(bean2);

    final List<BSimpleFor> beans = DB.find(BSimpleFor.class).findList();

    DbJson.of(beans)
      //.withPlaceholder("_")
      .replace("id", "whenModified")
      .assertContentMatches("/bean/example-list.json");
  }

  @Test
  public void assertContains_pass() {

    BSimpleFor bean = new BSimpleFor("something-contains-me", "YeahNah");
    DB.save(bean);

    BSimpleFor found = DB.find(BSimpleFor.class, bean.getId());

    DbJson.of(found).assertContainsResource("/bean/contains-minimal.json");
    DbJson.of(found).assertContains(readResource("/bean/contains-with-version.json"));

    DB.delete(bean);
  }

  @Test
  public void asJson() {

    BSimpleFor bean = new BSimpleFor("other");
    DB.save(bean);

    String asJson = DbJson.of(bean)
      .withPlaceholder("\"*Replaced*\"")
      .replace("id")
      .asJson();

    JsonNode node = Json.readNode(asJson);
    assertThat(node.get("id").asText()).isEqualTo("*Replaced*");
    assertThat(node.get("name").asText()).isEqualTo("other");

    DB.delete(bean);
  }
}
