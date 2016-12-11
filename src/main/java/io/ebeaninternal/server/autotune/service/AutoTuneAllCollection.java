package io.ebeaninternal.server.autotune.service;

import io.ebeaninternal.server.autotune.model.Autotune;

import java.util.Collection;

/**
 * Event where all tuned query information is collected.
 * <p>
 * This is for writing the "all" file on shutdown when using runtime tuning.
 * </p>
 */
public class AutoTuneAllCollection {

  final Autotune document = new Autotune();

  final BaseQueryTuner queryTuner;

  /**
   * Construct to collect/report all tuned queries.
   */
  public AutoTuneAllCollection(BaseQueryTuner queryTuner) {
    this.queryTuner = queryTuner;
    loadAllTuned();
  }

  /**
   * Return the number of origin elements in the document.
   */
  public int size() {
    return document.getOrigin().size();
  }

  /**
   * Return the Autotune document object.
   */
  public Autotune getDocument() {
    return document;
  }

  /**
   * Write the document as an xml file.
   */
  public void writeFile(String filePrefix, boolean withNow) {

    AutoTuneXmlWriter writer = new AutoTuneXmlWriter();
    writer.write(document, filePrefix, withNow);
  }

  /**
   * Loads all the existing query tuning into the document.
   */
  private void loadAllTuned() {

    Collection<TunedQueryInfo> all = queryTuner.getAll();
    for (TunedQueryInfo tuned : all) {
      document.getOrigin().add(tuned.getOrigin());
    }
  }

}
