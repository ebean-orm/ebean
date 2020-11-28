package io.ebeaninternal.server.readaudit;

import io.ebean.event.readaudit.ReadAuditLogger;
import io.ebean.event.readaudit.ReadAuditQueryPlan;
import io.ebean.event.readaudit.ReadEvent;
import io.ebean.text.json.EJson;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Default implementation of ReadAuditLogger that writes the event in JSON format to standard loggers.
 */
public class DefaultReadAuditLogger implements ReadAuditLogger {

  private static final Logger appLogger = LoggerFactory.getLogger(DefaultReadAuditLogger.class);

  private static final Logger queryLogger = LoggerFactory.getLogger("io.ebean.ReadAuditQuery");

  private static final Logger auditLogger = LoggerFactory.getLogger("io.ebean.ReadAudit");

  protected final JsonFactory jsonFactory = new JsonFactory();

  protected int defaultQueryBuffer = 500;

  protected int defaultReadBuffer = 150;

  /**
   * Write the query plan details in JSON format to the logger.
   */
  @Override
  public void queryPlan(ReadAuditQueryPlan queryPlan) {
    StringWriter writer = new StringWriter(defaultQueryBuffer);
    try (JsonGenerator gen = jsonFactory.createGenerator(writer)) {

      gen.writeStartObject();
      String beanType = queryPlan.getBeanType();
      if (beanType != null) {
        gen.writeStringField("beanType", beanType);
      }
      String queryKey = queryPlan.getQueryKey();
      if (queryKey != null) {
        gen.writeStringField("queryKey", queryKey);
      }
      String sql = queryPlan.getSql();
      if (sql != null) {
        gen.writeStringField("sql", sql);
      }
      gen.writeEndObject();
      gen.flush();

      queryLogger.info(writer.toString());

    } catch (IOException e) {
      appLogger.error("Error writing Read audit event", e);
    }
  }

  /**
   * Write the bean read event details in JSON format to the logger.
   */
  @Override
  public void auditBean(ReadEvent beanEvent) {

    writeEvent(beanEvent);
  }

  /**
   * Write the many beans read event details in JSON format to the logger.
   */
  @Override
  public void auditMany(ReadEvent readMany) {
    writeEvent(readMany);
  }

  protected void writeEvent(ReadEvent event) {

    try {
      StringWriter writer = new StringWriter(defaultReadBuffer);
      JsonGenerator gen = jsonFactory.createGenerator(writer);
      writeDetails(gen, event);

      auditLogger.info(writer.toString());

    } catch (IOException e) {
      appLogger.error("Error writing Read audit event", e);
    }
  }

  /**
   * Write the details for the read bean or read many beans event.
   */
  protected void writeDetails(JsonGenerator gen, ReadEvent event) throws IOException {

    gen.writeStartObject();
    String source = event.getSource();
    if (source != null) {
      gen.writeStringField("source", source);
    }
    String userId = event.getUserId();
    if (userId != null) {
      gen.writeStringField("userId", userId);
    }
    String userIpAddress = event.getUserIpAddress();
    if (userIpAddress != null) {
      gen.writeStringField("userIpAddress", userIpAddress);
    }
    Map<String, String> userContext = event.getUserContext();
    if (userContext != null && !userContext.isEmpty()) {
      gen.writeObjectFieldStart("userContext");
      for (Map.Entry<String, String> entry : userContext.entrySet()) {
        gen.writeStringField(entry.getKey(), entry.getValue());
      }
      gen.writeEndObject();
    }
    gen.writeNumberField("eventTime", event.getEventTime());
    gen.writeStringField("beanType", event.getBeanType());
    gen.writeStringField("queryKey", event.getQueryKey());
    gen.writeStringField("bindLog", event.getBindLog());
    Object id = event.getId();
    if (id != null) {
      gen.writeFieldName("id");
      EJson.write(id, gen);
    } else {
      gen.writeFieldName("ids");
      EJson.writeCollection(event.getIds(), gen);
    }

    gen.writeEndObject();
    gen.flush();
    gen.close();
  }

}
