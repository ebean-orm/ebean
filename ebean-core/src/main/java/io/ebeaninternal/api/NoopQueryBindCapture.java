package io.ebeaninternal.api;

import io.ebeaninternal.server.type.bindcapture.BindCapture;

class NoopQueryBindCapture implements SpiQueryBindCapture {

  @Override
  public boolean collectFor(long timeMicros) {
    return false;
  }

  @Override
  public void setBind(BindCapture bindCapture, long timeMicros, long startNanos) {
    // do nothing
  }

  @Override
  public void queryPlanInit(long thresholdMicros) {
    // do nothing
  }
}
