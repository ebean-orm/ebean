package com.avaje.ebeaninternal.server.changelog;

import com.avaje.ebean.ValuePair;
import com.avaje.ebean.event.changelog.BeanChange;
import com.avaje.ebean.event.changelog.ChangeSet;
import com.avaje.ebean.event.changelog.ChangeType;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonScalar;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * Builds JSON appropriate for loading into ElasticS via the bulk API.
 */
public class BulkJsonBuilder {

  protected final JsonFactory jsonFactory = new JsonFactory();

  protected final String indexName;

  protected final String indexType;

  protected final JsonContext json;

  protected BulkJsonBuilder(JsonContext json, String indexName, String indexType) {
    this.json = json;
    this.indexName = indexName;
    this.indexType = indexType;
  }

  /**
   * Write the change set into Elastic bulk API JSON form (so contains special new line
   * characters and bulk API header etc.
   */
  public void writeJson(ChangeSet changeSet, Writer writer) throws IOException {

    JsonGenerator generator = jsonFactory.createGenerator(writer);

    List<BeanChange> changes = changeSet.getChanges();
    for (int i = 0; i < changes.size(); i++) {
      write(generator, changes.get(i), changeSet, i);
    }

    generator.flush();
    generator.close();
  }

  /**
   * Write the bean change as a single JSON document for storage into Elastic.
   * <p>
   * Note that for ease of search/use we effectively denormalise by including the transaction header
   * information in each bean document.
   * </p>
   */
  protected void write(JsonGenerator gen, BeanChange bean, ChangeSet changeSet, int position) throws IOException {

    writeBulkHeader(gen, changeSet, position);
    writeBeanChange(gen, bean, changeSet);
    writeBeanChangeEnd(gen);
  }

  /**
   * Write the bean change as JSON document containing the transaction header details.
   */
  protected void writeBeanChange(JsonGenerator gen, BeanChange bean, ChangeSet changeSet) throws IOException {

    gen.writeStartObject();

    writeBeanTransactionDetails(gen, changeSet);

    gen.writeStringField("object", bean.getTable());
    gen.writeStringField("objectId", bean.getId().toString());
    gen.writeStringField("change", bean.getType().getCode());
    gen.writeNumberField("eventTime", bean.getEventTime());

    writeBeanValues(gen, bean);

    gen.writeEndObject();
  }

  /**
   * For Elastic bulk we append raw new line character.
   */
  protected void writeBeanChangeEnd(JsonGenerator gen) throws IOException {
    gen.writeRawValue("\n");
  }

  /**
   * Denormalise by writing the transaction header details.
   */
  protected void writeBeanTransactionDetails(JsonGenerator gen, ChangeSet changeSet) throws IOException {

    gen.writeStringField("txnId", changeSet.getTxnId());
    gen.writeStringField("txnState", changeSet.getTxnState().getCode());
    gen.writeNumberField("txnBatch", changeSet.getTxnBatch());
    gen.writeStringField("userId", changeSet.getUserId());
    String userIpAddress = changeSet.getUserIpAddress();
    if (userIpAddress != null) {
      gen.writeStringField("userIpAddress", userIpAddress);
    }
    String userContext = changeSet.getUserContext();
    if (userContext != null) {
      gen.writeStringField("userContext", userContext);
    }
  }

  /**
   * Write the elastic bulk API header.
   */
  protected void writeBulkHeader(JsonGenerator gen, ChangeSet changeSet, int position) throws IOException {

    // we index with an 'id' value so that we can process/reprocess the JSON and
    // avoid duplicates being inserted. Appending the batch and position give us
    // an effectively unique id value for the change
    String uid = changeSet.getTxnId() + "_" + changeSet.getTxnBatch() + "." + position;

    // the 'header' for elastic bulk API
    gen.writeStartObject();
    gen.writeFieldName("index");
    gen.writeStartObject();
    gen.writeStringField("_index", indexName);
    gen.writeStringField("_type", indexType);
    gen.writeStringField("_id", uid);
    gen.writeEndObject();
    gen.writeEndObject();
    gen.writeRawValue("\n");
  }

  /**
   * For insert and update write the new/old values.
   */
  protected void writeBeanValues(JsonGenerator gen, BeanChange bean) throws IOException {
    if (bean.getType() != ChangeType.DELETE) {
      gen.writeFieldName("values");
      gen.writeStartObject();
      // use JsonScalar as it knows how to encode all the scalar
      // property types that Ebean supports (Java8, Joda etc)
      JsonScalar scalarWriter = json.getScalar(gen);
      writeValuePairs(bean, scalarWriter, gen);
      gen.writeEndObject();
    }
  }

  /**
   * Write all the value pairs suppressing null values.
   * <p>
   * We are intentionally keeping the same new/old structure for both inserts and updates.
   * </p>
   */
  protected void writeValuePairs(BeanChange bean, JsonScalar scalarWriter, JsonGenerator gen) throws IOException {

    for (Map.Entry<String, ValuePair> entry : bean.getValues().entrySet()) {
      gen.writeFieldName(entry.getKey());
      gen.writeStartObject();
      ValuePair value = entry.getValue();
      Object newValue = value.getNewValue();
      if (newValue != null) {
        scalarWriter.write("new", newValue);
      }
      Object oldValue = value.getOldValue();
      if (oldValue != null) {
        scalarWriter.write("old", oldValue);
      }
      gen.writeEndObject();
    }
  }

}
