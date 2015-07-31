package com.avaje.ebeaninternal.server.lib.util;

/**
 * Listens to see if the message was successfully sent.
 */
public interface MailListener {

  /**
   * Handle the message event.
   */
  void handleEvent(MailEvent event);

}
