package com.avaje.ebeaninternal.server.autofetch.service;

import com.avaje.ebeaninternal.server.autofetch.AutoTuneCollection;
import com.avaje.ebeaninternal.server.util.LongAdder;

import java.io.Serializable;

/**
 * Used to accumulate query execution statistics for paths relative to the origin query.
 */
public class ProfileOriginQuery implements Serializable {

  private static final long serialVersionUID = -1133958958072778811L;

  private final String path;

  private final LongAdder exeCount = new LongAdder();

  private final LongAdder totalBeanLoaded = new LongAdder();

  private final LongAdder totalMicros = new LongAdder();

  public ProfileOriginQuery(String path) {
    this.path = path;
  }

  public void add(long beansLoaded, long micros) {
    exeCount.increment();
    totalBeanLoaded.add(beansLoaded);
    totalMicros.add(micros);
  }

  public AutoTuneCollection.EntryQuery createEntryQuery(boolean reset){

    if (reset) {
      return new AutoTuneCollection.EntryQuery(path, exeCount.sumThenReset(), totalBeanLoaded.sumThenReset(), totalMicros.sumThenReset());

    } else {
      return new AutoTuneCollection.EntryQuery(path, exeCount.sum(), totalBeanLoaded.sum(), totalMicros.sum());
    }
  }

}