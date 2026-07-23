package org.example.avajejsonb;

import io.avaje.json.node.JsonNode;
import io.ebean.avajejsonb.mapper.JsonbPayload;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonB;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.List;

@Entity
public class JsonbEntity {

  @Id
  long id;

  @DbJson
  JsonbPayload payload;

  @DbJson
  List<JsonbPayload> payloads;

  @DbJsonB
  JsonNode node;

  public long getId() {
    return id;
  }

  public JsonbPayload getPayload() {
    return payload;
  }

  public void setPayload(JsonbPayload payload) {
    this.payload = payload;
  }

  public List<JsonbPayload> getPayloads() {
    return payloads;
  }

  public void setPayloads(List<JsonbPayload> payloads) {
    this.payloads = payloads;
  }

  public JsonNode getNode() {
    return node;
  }

  public void setNode(JsonNode node) {
    this.node = node;
  }
}
