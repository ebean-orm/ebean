package io.ebean.avajejsonb.mapper;

import io.avaje.json.node.JsonNode;
import io.avaje.json.node.JsonObject;
import io.avaje.jsonb.Json;
import io.ebean.annotation.MutationDetection;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarJsonManager;
import io.ebean.core.type.ScalarJsonRequest;
import io.ebean.core.type.ScalarType;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class ScalarJsonAvajeJsonbMapperTest {

  private static final ScalarJsonManager JSON_MANAGER = new ScalarJsonManager() {

    @Override
    public MutationDetection mutationDetection() {
      return MutationDetection.HASH;
    }

    @Override
    public Object mapper() {
      return null;
    }

    @Override
    public String postgresType(int dbType) {
      return null;
    }
  };

  private final ScalarJsonAvajeJsonbMapper mapper = new ScalarJsonAvajeJsonbMapper();

  @Test
  void pojo_roundTripsThroughGeneratedJsonbAdapter() {
    ScalarType<Object> scalarType = scalarType("document", MutationDetection.HASH);
    Payload payload = new Payload("hello", 42);

    String json = scalarType.formatValue(payload);

    assertThat(json).isEqualTo("{\"name\":\"hello\",\"count\":42}");
    assertThat(scalarType.parse(json)).isEqualTo(payload);
    assertThat(scalarType.mutable()).isTrue();
    assertThat(scalarType.jsonMapper()).isTrue();
  }

  @Test
  void genericList_roundTripsUsingPropertyGenericType() {
    ScalarType<Object> scalarType = scalarType("payloads", MutationDetection.HASH);
    List<Payload> payloads = List.of(new Payload("one", 1), new Payload("two", 2));

    String json = scalarType.formatValue(payloads);

    assertThat(json).isEqualTo("[{\"name\":\"one\",\"count\":1},{\"name\":\"two\",\"count\":2}]");
    assertThat(scalarType.parse(json)).isEqualTo(payloads);
  }

  @Test
  void jsonNode_roundTripsThroughAvajeJsonNodeComponent() {
    ScalarType<Object> scalarType = scalarType("node", MutationDetection.HASH);
    JsonNode node = JsonObject.create().add("name", "node").add("count", 3);

    String json = scalarType.formatValue(node);

    assertThat(json).isEqualTo("{\"name\":\"node\",\"count\":3}");
    JsonNode parsed = (JsonNode) scalarType.parse(json);
    assertThat(parsed.extract("name")).isEqualTo("node");
    assertThat(parsed.extract("count", 0)).isEqualTo(3);
  }

  @Test
  void mutationDetectionNone_isNotMutable() {
    ScalarType<Object> scalarType = scalarType("document", MutationDetection.NONE);

    assertThat(scalarType.mutable()).isFalse();
    assertThat(scalarType.jsonMapper()).isFalse();
  }

  @SuppressWarnings("unchecked")
  private ScalarType<Object> scalarType(String property, MutationDetection mutationDetection) {
    var request = new ScalarJsonRequest(JSON_MANAGER, Types.VARCHAR, DocPropertyType.OBJECT, Entity.class, mutationDetection, property);
    return (ScalarType<Object>) mapper.createType(request);
  }

  private static final class Entity {

    Payload document;
    List<Payload> payloads;
    JsonNode node;
  }

  @Json
  static class Payload {

    public String name;
    public int count;

    Payload() {
    }

    Payload(String name, int count) {
      this.name = name;
      this.count = count;
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) {
        return true;
      }
      if (!(object instanceof Payload)) {
        return false;
      }
      Payload other = (Payload) object;
      return count == other.count && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, count);
    }
  }
}
