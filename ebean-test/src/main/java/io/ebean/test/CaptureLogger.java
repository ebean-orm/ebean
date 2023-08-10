package io.ebean.test;

import io.ebeaninternal.api.SpiLogger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Capture the log messages (executed SQL) for testing.
 */
final class CaptureLogger implements SpiLogger {

  private final SpiLogger wrapped;
  private List<String> messages = new ArrayList<>();
  private boolean active;

  CaptureLogger(SpiLogger wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public boolean isDebug() {
    return true;
  }

  @Override
  public void debug(String msg, Object... args) {
    if (active) {
      if (args != null && args.length > 0) {
        messages.add(MessageFormat.format(msg, args));
      } else {
        messages.add(msg);
      }
    }
    wrapped.debug(msg, args);
  }

  List<String> start() {
    this.active = true;
    return collect();
  }

  List<String> stop() {
    this.active = false;
    return collect();
  }

  List<String> collect() {
    List<String> response = messages;
    messages = new ArrayList<>();
    return response;
  }
}
