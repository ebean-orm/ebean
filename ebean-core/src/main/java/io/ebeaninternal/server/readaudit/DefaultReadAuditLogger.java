package io.ebeaninternal.server.readaudit;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.avaje.applog.AppLog;
import io.ebean.event.readaudit.ReadAuditLogger;
import io.ebean.event.readaudit.ReadAuditQueryPlan;
import io.ebean.event.readaudit.ReadEvent;
import io.ebean.text.json.EJson;
import io.ebeaninternal.api.CoreLog;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

/**
 * Default implementation of ReadAuditLogger that writes the event in JSON format to standard loggers.
 */
public class DefaultReadAuditLogger implements ReadAuditLogger {

  private static final System.Logger queryLogger = AppLog.getLogger("io.ebean.ReadAuditQuery");
  private static final System.Logger auditLogger = AppLog.getLogger("io.ebean.ReadAudit");

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
      queryLogger.log(INFO, writer.toString());
    } catch (IOException e) {
      CoreLog.log.log(ERROR, "Error writing Read audit event", e);
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
      auditLogger.log(INFO, writer.toString());
    } catch (IOException e) {
      CoreLog.log.log(ERROR, "Error writing Read audit event", e);
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
