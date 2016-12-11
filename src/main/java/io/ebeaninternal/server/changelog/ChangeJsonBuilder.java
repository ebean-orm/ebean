package io.ebeaninternal.server.changelog;

import io.ebean.ValuePair;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeSet;
import io.ebean.event.changelog.ChangeType;
import io.ebean.text.json.JsonContext;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Builds JSON document for a bean change.
 */
public class ChangeJsonBuilder {

  protected final JsonFactory jsonFactory = new JsonFactory();

  protected final JsonContext json;

  protected ChangeJsonBuilder(JsonContext json) {
    this.json = json;
  }

  /**
   * Write the bean change as JSON.
   */
  public void writeBeanJson(Writer writer, BeanChange bean, ChangeSet changeSet, int position) throws IOException {

    JsonGenerator generator = jsonFactory.createGenerator(writer);

    writeBeanChange(generator, bean, changeSet, position);

    generator.flush();
    generator.close();
  }

  /**
   * Write the bean change as JSON document containing the transaction header details.
   */
  protected void writeBeanChange(JsonGenerator gen, BeanChange bean, ChangeSet changeSet, int position) throws IOException {

    gen.writeStartObject();

    writeBeanTransactionDetails(gen, changeSet, position);

    gen.writeStringField("object", bean.getTable());
    gen.writeStringField("objectId", bean.getId().toString());
    gen.writeStringField("change", bean.getType().getCode());
    gen.writeNumberField("eventTime", bean.getEventTime());

    writeBeanValues(gen, bean);

    gen.writeEndObject();
  }

  /**
   * Denormalise by writing the transaction header details.
   */
  protected void writeBeanTransactionDetails(JsonGenerator gen, ChangeSet changeSet, int position) throws IOException {

    gen.writeStringField("txnId", changeSet.getTxnId());
    gen.writeStringField("txnState", changeSet.getTxnState().getCode());
    gen.writeNumberField("txnBatch", changeSet.getTxnBatch());
    gen.writeNumberField("txnPosition", position);
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
  protected void writeBeanValues(JsonGenerator gen, BeanChange bean) throws IOException {

    if (bean.getType() != ChangeType.DELETE) {
      gen.writeFieldName("values");
      gen.writeStartObject();
      writeValuePairs(bean, gen);
      gen.writeEndObject();
    }
  }

  /**
   * Write all the value pairs suppressing null values.
   * <p>
   * We are intentionally keeping the same new/old structure for both inserts and updates.
   * </p>
   */
  protected void writeValuePairs(BeanChange bean, JsonGenerator gen) throws IOException {

    for (Map.Entry<String, ValuePair> entry : bean.getValues().entrySet()) {
      gen.writeFieldName(entry.getKey());
      gen.writeStartObject();
      ValuePair value = entry.getValue();
      Object newValue = value.getNewValue();
      if (newValue != null) {
        gen.writeFieldName("new");
        json.writeScalar(gen, newValue);
      }
      Object oldValue = value.getOldValue();
      if (oldValue != null) {
        gen.writeFieldName("old");
        json.writeScalar(gen, oldValue);
      }
      gen.writeEndObject();
    }
  }

}
