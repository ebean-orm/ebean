package io.ebeaninternal.server.autotune;

import io.ebeaninternal.api.SpiQuery;

/**
 * Noop service when AutoTuneService is not available.
 */
public class NoAutoTuneService implements AutoTuneService {

  @Override
  public void startup() {
    // do nothing
  }

  @Override
  public boolean tuneQuery(SpiQuery<?> query) {
    return false;
  }

  @Override
  public void collectProfiling() {
    // do nothing
  }

  @Override
  public void reportProfiling() {
    // do nothing
  }

  @Override
  public void shutdown() {
    // do nothing
  }
}
