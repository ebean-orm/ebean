package io.ebeaninternal.server.readaudit;

import io.avaje.applog.AppLog;
import io.avaje.json.JsonWriter;
import io.avaje.json.stream.JsonStream;
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

  protected final JsonStream jsonStream = JsonStream.builder().build();
  protected final int defaultQueryBuffer = 500;
  protected final int defaultReadBuffer = 150;

  /**
   * Write the query plan details in JSON format to the logger.
   */
  @Override
  public void queryPlan(ReadAuditQueryPlan queryPlan) {
    StringWriter writer = new StringWriter(defaultQueryBuffer);
    try (JsonWriter gen = jsonStream.writer(writer)) {
      gen.beginObject();
      String beanType = queryPlan.getBeanType();
      if (beanType != null) {
        gen.name("beanType");
        gen.value(beanType);
      }
      String queryKey = queryPlan.getQueryKey();
      if (queryKey != null) {
        gen.name("queryKey");
        gen.value(queryKey);
      }
      String sql = queryPlan.getSql();
      if (sql != null) {
        gen.name("sql");
        gen.value(sql);
      }
      gen.endObject();
      gen.flush();
      queryLogger.log(INFO, writer.toString());
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
    StringWriter writer = new StringWriter(defaultReadBuffer);
    try (JsonWriter gen = jsonStream.writer(writer)) {
      writeDetails(gen, event);
    } catch (IOException e) {
      CoreLog.log.log(ERROR, "Error writing Read audit event", e);
      return;
    }
    auditLogger.log(INFO, writer.toString());
  }

  /**
   * Write the details for the read bean or read many beans event.
   */
  protected void writeDetails(JsonWriter gen, ReadEvent event) throws IOException {
    gen.beginObject();
    String source = event.getSource();
    if (source != null) {
      gen.name("source");
      gen.value(source);
    }
    String userId = event.getUserId();
    if (userId != null) {
      gen.name("userId");
      gen.value(userId);
    }
    String userIpAddress = event.getUserIpAddress();
    if (userIpAddress != null) {
      gen.name("userIpAddress");
      gen.value(userIpAddress);
    }
    Map<String, String> userContext = event.getUserContext();
    if (userContext != null && !userContext.isEmpty()) {
      gen.name("userContext");
      gen.beginObject();
      for (Map.Entry<String, String> entry : userContext.entrySet()) {
        gen.name(entry.getKey());
        gen.value(entry.getValue());
      }
      gen.endObject();
    }
    gen.name("eventTime");
    gen.value(event.getEventTime());
    gen.name("beanType");
    gen.value(event.getBeanType());
    gen.name("queryKey");
    gen.value(event.getQueryKey());
    gen.name("bindLog");
    gen.value(event.getBindLog());
    Object id = event.getId();
    if (id != null) {
      gen.name("id");
      EJson.write(id, gen);
    } else {
      gen.name("ids");
      EJson.writeCollection(event.getIds(), gen);
    }
    gen.endObject();
    gen.flush();
  }

}
