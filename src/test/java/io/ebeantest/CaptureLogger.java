package io.ebeantest;

import io.ebeaninternal.api.SpiLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Capture the log messages (executed SQL) for testing.
 */
class CaptureLogger implements SpiLogger {

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
  public boolean isTrace() {
    return true;
  }

  @Override
  public void debug(String msg) {
    if (active) {
      messages.add(msg);
    }
    wrapped.debug(msg);
  }

  @Override
  public void trace(String msg) {
    if (active) {
      messages.add(msg);
    }
    wrapped.trace(msg);
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
