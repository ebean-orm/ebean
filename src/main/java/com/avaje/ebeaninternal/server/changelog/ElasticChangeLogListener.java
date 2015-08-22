package com.avaje.ebeaninternal.server.changelog;

import com.avaje.ebean.event.changelog.ChangeLogListener;
import com.avaje.ebean.event.changelog.ChangeSet;
import com.avaje.ebean.plugin.SpiServer;
import com.avaje.ebean.plugin.SpiServerPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Logs the change sets in a Elastic Search Bulk API JSON format.
 */
public class ElasticChangeLogListener implements ChangeLogListener, SpiServerPlugin {

  protected static final Logger appLog = LoggerFactory.getLogger(ElasticChangeLogListener.class);

  protected static final Logger fileLog = LoggerFactory.getLogger("org.avaje.ebean.ElasticChangeLog");

  protected BulkJsonBuilder jsonBuilder;

  public ElasticChangeLogListener() {
  }

  /**
   * Configure the underlying JSON handler.
   */
  @Override
  public void configure(SpiServer server) {
    jsonBuilder = new BulkJsonBuilder(server.json(), "changelog", "changelog");
  }

  @Override
  public void online(boolean online) {
    // We don't care online or offline in this case
    // we might if we setup for network sending etc
  }

  @Override
  public void log(ChangeSet changeSet) {

    try {
      // I'm pretty sure I'm going to change this to use a  FileWriter (but apache kafta could be good here too)
      // This buffer could get really big and to me normal logging (without a Writer) is not that well suited to
      // this problem so ... works but lets do better here and write direct to files (without the buffer issue)
      StringWriter writer = new StringWriter(getBufferSize(changeSet));
      jsonBuilder.writeJson(changeSet, writer);
      String json = writer.toString();

      fileLog.info("Sending txnId:{} txnState:{} txnBatch:{}  \n {}", changeSet.getTxnId(), changeSet.getTxnState(), changeSet.getTxnBatch(), json);

    } catch (IOException e) {
      String msg = extractErrorMessage(e);
      fileLog.error("Exception sending txnId:{} txnState:{} txnBatch:{} error:{}", changeSet.getTxnId(), changeSet.getTxnState(), changeSet.getTxnBatch(), msg);
      appLog.error("Exception sending changeSet "+changeSet.toString(), e);
    }
  }

  protected int getBufferSize(ChangeSet changeSet) {
    // a rough guess, could get smarter here
    return Math.min(400 * changeSet.size(), 3000);
  }

  /**
   * Extract an error message that does not have new line characters and hence safe to go into
   * our log which contains the payloads (that we want to be able to extract easily later on).
   */
  @NotNull
  protected String extractErrorMessage(Exception e) {

    String msg = e.toString();
    msg = msg.replace('\r','|');
    msg = msg.replace('\n', '|');
    return msg;
  }

}
