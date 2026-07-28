package io.ebean.avajejsonb.mapper;

import io.avaje.json.node.JsonNode;
import io.avaje.json.node.JsonObject;
import io.ebean.Database;
import io.ebean.DatabaseBuilder;
import org.example.avajejsonb.JsonbEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class JsonbDatabaseTest {

  @Test
  void dbJson_roundTripsJsonbPayloadsAndJsonNode() {
    Database database = buildDatabase();
    try {
      JsonbEntity entity = new JsonbEntity();
      entity.setPayload(new JsonbPayload("main", 1));
      entity.setPayloads(List.of(new JsonbPayload("first", 2), new JsonbPayload("second", 3)));
      entity.setNode(JsonObject.create().add("name", "node").add("count", 4));
      database.save(entity);

      JsonbEntity found = database.find(JsonbEntity.class, entity.getId());

      assertThat(found.getPayload()).isEqualTo(new JsonbPayload("main", 1));
      assertThat(found.getPayloads()).containsExactly(new JsonbPayload("first", 2), new JsonbPayload("second", 3));
      JsonNode node = found.getNode();
      assertThat(node.extract("name")).isEqualTo("node");
      assertThat(node.extract("count", 0)).isEqualTo(4);
    } finally {
      database.shutdown();
    }
  }

  private static Database buildDatabase() {
    DatabaseBuilder config = Database.builder();
    config.setName("avajeJsonbMapper");
    config.setDefaultServer(false);
    config.setDdlGenerate(true);
    config.setDdlRun(true);
    config.setDdlExtra(false);

    Properties properties = new Properties();
    properties.setProperty("datasource.avajeJsonbMapper.username", "sa");
    properties.setProperty("datasource.avajeJsonbMapper.password", "");
    properties.setProperty("datasource.avajeJsonbMapper.databaseUrl", "jdbc:h2:mem:avajeJsonbMapper");
    properties.setProperty("datasource.avajeJsonbMapper.databaseDriver", "org.h2.Driver");
    config.loadFromProperties(properties);
    config.addClass(JsonbEntity.class);
    return config.build();
  }
}
