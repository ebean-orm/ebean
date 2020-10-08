package io.ebeaninternal.server.changelog;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeSet;
import io.ebean.event.changelog.ChangeType;
import io.ebean.text.json.JsonContext;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Builds JSON document for a bean change.
 */
class ChangeJsonBuilder {

  protected final JsonFactory jsonFactory = new JsonFactory();

  protected final JsonContext json;

  ChangeJsonBuilder(JsonContext json) {
    this.json = json;
  }

  /**
   * Write the bean change as JSON.
   */
  void writeBeanJson(Writer writer, BeanChange bean, ChangeSet changeSet) throws IOException {

    try (JsonGenerator generator = jsonFactory.createGenerator(writer)) {
      writeBeanChange(generator, bean, changeSet);
      generator.flush();
    }
  }

  /**
   * Write the bean change as JSON document containing the transaction header details.
   */
  private void writeBeanChange(JsonGenerator gen, BeanChange bean, ChangeSet changeSet) throws IOException {

    gen.writeStartObject();

    gen.writeNumberField("ts", bean.getEventTime());
    gen.writeStringField("change", bean.getEvent().getCode());
    gen.writeStringField("type", bean.getType());
    gen.writeStringField("id", bean.getId().toString());
    if (bean.getTenantId() != null) {
      gen.writeStringField("tenantId", bean.getTenantId().toString());
    }

    writeBeanTransactionDetails(gen, changeSet);

    writeBeanValues(gen, bean);
    gen.writeEndObject();
  }

  /**
   * Denormalise by writing the transaction header details.
   */
  private void writeBeanTransactionDetails(JsonGenerator gen, ChangeSet changeSet) throws IOException {

    String source = changeSet.getSource();
    if (source != null) {
      gen.writeStringField("source", source);
    }
    String userId = changeSet.getUserId();
    if (userId != null) {
      gen.writeStringField("userId", userId);
    }
    String userIpAddress = changeSet.getUserIpAddress();
    if (userIpAddress != null) {
      gen.writeStringField("userIpAddress", userIpAddress);
    }
    Map<String, String> userContext = changeSet.getUserContext();
    if (userContext != null && !userContext.isEmpty()) {
      gen.writeObjectFieldStart("userContext");
      for (Map.Entry<String, String> entry : userContext.entrySet()) {
        gen.writeStringField(entry.getKey(), entry.getValue());
      }
      gen.writeEndObject();
    }
  }

  /**
   * For insert and update write the new/old values.
   */
  private void writeBeanValues(JsonGenerator gen, BeanChange bean) throws IOException {

    if (bean.getEvent() != ChangeType.DELETE) {
      gen.writeFieldName("data");
      gen.writeRaw(":");
      gen.writeRaw(bean.getData());

      String oldData = bean.getOldData();
      if (oldData != null) {
        gen.writeRaw(",\"oldData\":");
        gen.writeRaw(oldData);
      }
    }
  }

}
