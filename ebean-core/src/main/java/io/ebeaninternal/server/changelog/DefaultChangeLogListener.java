package io.ebeaninternal.server.changelog;

import io.avaje.applog.AppLog;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeLogListener;
import io.ebean.event.changelog.ChangeSet;
import io.ebean.event.changelog.ChangeType;
import io.ebean.plugin.Plugin;
import io.ebean.plugin.SpiServer;
import io.ebeaninternal.api.CoreLog;

import java.io.StringWriter;
import java.util.Properties;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

/**
 * Simply logs the change sets in JSON form to logger named <code>io.ebean.ChangeLog</code>.
 */
public final class DefaultChangeLogListener implements ChangeLogListener, Plugin {

  /**
   * The named logger we send the change set payload to. Can be externally configured as desired.
   */
  private static final System.Logger changeLog = AppLog.getLogger("io.ebean.ChangeLog");

  /**
   * Used to build the JSON.
   */
  private ChangeJsonBuilder jsonBuilder;

  /**
   * A bigger default buffer for bean inserts and updates (that have value pairs).
   */
  private int defaultBufferSize = 400;

  /**
   * Configure the underlying JSON handler.
   */
  @Override
  public void configure(SpiServer server) {
    jsonBuilder = new ChangeJsonBuilder();
    Properties properties = server.config().getProperties();
    if (properties != null) {
      String bufferSize = properties.getProperty("ebean.changeLog.bufferSize");
      if (bufferSize != null) {
        defaultBufferSize = Integer.parseInt(bufferSize);
      }
    }
  }

  @Override
  public void online(boolean online) {
    // nothing to do
  }

  @Override
  public void shutdown() {
    // nothing to do
  }

  @Override
  public void log(ChangeSet changeSet) {
    for (BeanChange beanChange : changeSet.getChanges()) {
      // log each bean change as a separate log entry
      try {
        StringWriter writer = new StringWriter(getBufferSize(beanChange));
        jsonBuilder.writeBeanJson(writer, beanChange, changeSet);
        changeLog.log(INFO, writer.toString());
      } catch (Exception e) {
        CoreLog.log.log(ERROR, "Exception logging beanChange " + beanChange, e);
      }
    }
  }

  /**
   * Return a decent buffer size based on the bean change.
   */
  private int getBufferSize(BeanChange beanChange) {
    return ChangeType.DELETE == beanChange.getEvent() ? 250 : defaultBufferSize;
  }

}
