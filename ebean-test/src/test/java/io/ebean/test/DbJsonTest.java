package io.ebean.test;

import com.fasterxml.jackson.databind.JsonNode;
import io.ebean.DB;
import org.junit.Test;
import org.test.BSimpleWithGen;

import java.util.List;

import static io.ebean.test.DbJson.readResource;
import static org.assertj.core.api.Assertions.assertThat;


public class DbJsonTest {

  @Test
  public void of() {

    DB.find(BSimpleWithGen.class).delete();

    BSimpleWithGen bean = new BSimpleWithGen("something");
    DB.save(bean);

    assertThat(bean.getVersion()).isEqualTo(1);

    DbJson.of(bean)
      .withPlaceholder("_")
      .replace("id", "whenModified")
      .assertContentMatches("/bean/example-bean.json");


    BSimpleWithGen bean2 = new BSimpleWithGen("other");
    DB.save(bean2);

    final List<BSimpleWithGen> beans = DB.find(BSimpleWithGen.class).findList();

    DbJson.of(beans)
      //.withPlaceholder("_")
      .replace("id", "whenModified")
      .assertContentMatches("/bean/example-list.json");
  }

  @Test
  public void assertContains_pass() {

    BSimpleWithGen bean = new BSimpleWithGen("something-contains-me", "YeahNah");
    DB.save(bean);

    BSimpleWithGen found = DB.find(BSimpleWithGen.class, bean.getId());

    DbJson.of(found).assertContainsResource("/bean/contains-minimal.json");
    DbJson.of(found).assertContains(readResource("/bean/contains-with-version.json"));

    DB.delete(bean);
  }

  @Test
  public void asJson() {

    BSimpleWithGen bean = new BSimpleWithGen("other");
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
