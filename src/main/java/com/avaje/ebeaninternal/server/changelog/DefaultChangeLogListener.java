package com.avaje.ebeaninternal.server.changelog;

import com.avaje.ebean.event.changelog.BeanChange;
import com.avaje.ebean.event.changelog.ChangeLogListener;
import com.avaje.ebean.event.changelog.ChangeSet;
import com.avaje.ebean.event.changelog.ChangeType;
import com.avaje.ebean.plugin.Plugin;
import com.avaje.ebean.plugin.SpiServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

/**
 * Logs the change sets in JSON to logger named <code>org.avaje.ebean.ChangeLog</code>.
 * <p>
 * The logged entries duplicate/denormalise the transaction details so that each bean change
 * is fully contained with the transaction information.
 * </p>
 */
public class DefaultChangeLogListener implements ChangeLogListener, Plugin {

  /**
   * The usual application specific logger.
   */
  protected static final Logger logger = LoggerFactory.getLogger(DefaultChangeLogListener.class);

  /**
   * The named logger we send the change set payload to. Can be externally configured as desired.
   */
  protected static final Logger changeLog = LoggerFactory.getLogger("org.avaje.ebean.ChangeLog");

  /**
   * Used to build the JSON.
   */
  protected ChangeJsonBuilder jsonBuilder;

  /**
   * A bigger default buffer for bean inserts and updates (that have value pairs).
   */
  protected int defaultBufferSize = 400;

  /**
   * Expected to be a reasonable buffer size for deletes (which do not have value pairs).
   */
  protected int defaultDeleteBufferSize = 250;

  public DefaultChangeLogListener() {
  }

  /**
   * Configure the underlying JSON handler.
   */
  @Override
  public void configure(SpiServer server) {
    jsonBuilder = new ChangeJsonBuilder(server.json());

    Properties properties = server.getServerConfig().getProperties();
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

    List<BeanChange> changes = changeSet.getChanges();
    for (int i = 0; i < changes.size(); i++) {
      // log each bean change as a separate log entry
      BeanChange beanChange = changes.get(i);
      try {
        StringWriter writer = new StringWriter(getBufferSize(beanChange));
        jsonBuilder.writeBeanJson(writer, beanChange, changeSet, i);
        changeLog.info(writer.toString());
      } catch (Exception e) {
        logger.error("Exception logging beanChange " + beanChange.toString(), e);
      }
    }
  }

  /**
   * Return a decent buffer size based on the bean change.
   */
  protected int getBufferSize(BeanChange beanChange) {

    return ChangeType.DELETE == beanChange.getType() ? defaultDeleteBufferSize : defaultBufferSize;
  }

}
