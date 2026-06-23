package io.ebeaninternal.server.changelog;

import io.avaje.json.JsonWriter;
import io.avaje.json.stream.JsonStream;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeSet;
import io.ebean.event.changelog.ChangeType;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Builds JSON document for a bean change.
 */
final class ChangeJsonBuilder {

  private final JsonStream jsonStream = JsonStream.builder().build();

  /**
   * Write the bean change as JSON.
   */
  void writeBeanJson(Writer writer, BeanChange bean, ChangeSet changeSet) throws IOException {
    try (JsonWriter generator = jsonStream.writer(writer)) {
      writeBeanChange(generator, bean, changeSet);
      generator.flush();
    }
  }

  /**
   * Write the bean change as JSON document containing the transaction header details.
   */
  private void writeBeanChange(JsonWriter gen, BeanChange bean, ChangeSet changeSet) {
    gen.beginObject();
    gen.name("ts");
    gen.value(bean.getEventTime());
    gen.name("change");
    gen.value(bean.getEvent().getCode());
    gen.name("type");
    gen.value(bean.getType());
    gen.name("id");
    gen.value(bean.getId().toString());
    if (bean.getTenantId() != null) {
      gen.name("tenantId");
      gen.value(bean.getTenantId().toString());
    }
    writeBeanTransactionDetails(gen, changeSet);
    writeBeanValues(gen, bean);
    gen.endObject();
  }

  /**
   * Denormalise by writing the transaction header details.
   */
  private void writeBeanTransactionDetails(JsonWriter gen, ChangeSet changeSet) {
    String source = changeSet.getSource();
    if (source != null) {
      gen.name("source");
      gen.value(source);
    }
    String userId = changeSet.getUserId();
    if (userId != null) {
      gen.name("userId");
      gen.value(userId);
    }
    String userIpAddress = changeSet.getUserIpAddress();
    if (userIpAddress != null) {
      gen.name("userIpAddress");
      gen.value(userIpAddress);
    }
    Map<String, String> userContext = changeSet.getUserContext();
    if (userContext != null && !userContext.isEmpty()) {
      gen.name("userContext");
      gen.beginObject();
      for (Map.Entry<String, String> entry : userContext.entrySet()) {
        gen.name(entry.getKey());
        gen.value(entry.getValue());
      }
      gen.endObject();
    }
  }

  /**
   * For insert and update write the new/old values.
   */
  private void writeBeanValues(JsonWriter gen, BeanChange bean) {
    if (bean.getEvent() != ChangeType.DELETE) {
      gen.name("data");
      gen.rawValue(bean.getData());
      String oldData = bean.getOldData();
      if (oldData != null) {
        gen.name("oldData");
        gen.rawValue(oldData);
      }
    }
  }

}
