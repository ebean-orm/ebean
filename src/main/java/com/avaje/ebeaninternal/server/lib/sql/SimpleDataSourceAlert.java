package com.avaje.ebeaninternal.server.lib.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebeaninternal.server.lib.util.MailEvent;
import com.avaje.ebeaninternal.server.lib.util.MailListener;
import com.avaje.ebeaninternal.server.lib.util.MailMessage;
import com.avaje.ebeaninternal.server.lib.util.MailSender;

/**
 * A simple smtp email alert that sends a email message on dataSourceDown and
 * dataSourceUp etc.
 * <ul>
 * <li>alert.fromuser = the from user name
 * <li>alert.fromemail = the from email account
 * <li>alert.toemail = comma delimited list of email accounts to email
 * <li>alert.mailserver = the smpt server name
 * </ul>
 */
public class SimpleDataSourceAlert implements DataSourceAlert, MailListener {

  private static final Logger logger = LoggerFactory.getLogger(SimpleDataSourceAlert.class);

  // boolean sendInBackGround = true;

  /**
   * Create a SimpleAlerter.
   */
  public SimpleDataSourceAlert() {
  }

  /**
   * If the email failed then log the error.
   */
  public void handleEvent(MailEvent event) {
    Throwable e = event.getError();
    if (e != null) {
      logger.error(null, e);
    }
  }

  /**
   * Send the dataSource down alert.
   */
  @Override
  public void dataSourceDown(String dataSourceName) {
    String msg = getSubject(true, dataSourceName);
    sendMessage(msg, msg);
  }

  /**
   * Send the dataSource up alert.
   */
  @Override
  public void dataSourceUp(String dataSourceName) {
    String msg = getSubject(false, dataSourceName);
    sendMessage(msg, msg);
  }

  /**
   * Send the warning message.
   */
  @Override
  public void dataSourceWarning(String subject, String msg) {
    sendMessage(subject, msg);
  }

  private String getSubject(boolean isDown, String dsName) {
    String msg = "The DataSource " + dsName;
    if (isDown) {
      msg += " is DOWN!!";
    } else {
      msg += " is UP.";
    }
    return msg;
  }

  private void sendMessage(String subject, String msg) {

    String mailServerName = GlobalProperties.get("datasource.alert.mailserver", null);
    if (mailServerName == null) {
      return;
    }

    String fromUser = GlobalProperties.get("datasource.alert.fromuser", null);
    String fromEmail = GlobalProperties.get("datasource.alert.fromemail", null);
    String toEmail = GlobalProperties.get("datasource.alert.toemail", null);
    
    MailMessage data = new MailMessage();
    data.setSender(fromUser, fromEmail);
    data.addBodyLine(msg);
    data.setSubject(subject);

    String[] toList = toEmail.split(",");
    if (toList.length == 0) {
      throw new RuntimeException("alert.toemail has not been set?");
    }
    for (int i = 0; i < toList.length; i++) {
      data.addRecipient(null, toList[i].trim());
    }

    MailSender sender = new MailSender(mailServerName);
    sender.setMailListener(this);
    sender.sendInBackground(data);
  }

}
