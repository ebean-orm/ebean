package io.ebeaninternal.server.autotune.service;

import io.ebean.bean.ObjectGraphOrigin;
import io.ebeaninternal.server.autotune.model.Autotune;
import io.ebeaninternal.server.autotune.model.Origin;
import io.ebeaninternal.server.autotune.model.ProfileDiff;
import io.ebeaninternal.server.autotune.model.ProfileNew;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;

/**
 * Event where profiling information is collected and processed for differences
 * relative to the current query tuning.
 */
public class AutoTuneDiffCollection {

  final Autotune document = new Autotune();

  final AutoTuneCollection profiling;

  final BaseQueryTuner queryTuner;

  final boolean updateTuning;

  int newCount;

  int diffCount;

  /**
   * Construct to collect/report the new/diff query tuning entries.
   */
  public AutoTuneDiffCollection(AutoTuneCollection profiling, BaseQueryTuner queryTuner, boolean updateTuning) {
    this.profiling = profiling;
    this.queryTuner = queryTuner;
    this.updateTuning = updateTuning;
  }

  /**
   * Return true if there are no new or diff entries.
   */
  public boolean isEmpty() {
    return newCount == 0 && diffCount == 0;
  }

  /**
   * Return the underlying Autotune document object.
   */
  public Autotune getDocument() {
    return document;
  }

  /**
   * Return the number of diff entries.
   */
  public int getDiffCount() {
    return diffCount;
  }

  /**
   * Return the number of new entries.
   */
  public int getNewCount() {
    return newCount;
  }

  /**
   * Return the total new and diff entries.
   */
  public int getChangeCount() {
    return newCount + diffCount;
  }

  /**
   * Write the underlying document as an xml file.
   */
  public void writeFile(String filePrefix) {

    AutoTuneXmlWriter writer = new AutoTuneXmlWriter();
    writer.write(document, filePrefix, true);
  }

  /**
   * Process checking profiling entries against existing query tuning.
   */
  public void process() {

    for (AutoTuneCollection.Entry entry : profiling.getEntries()) {
      addToDocument(entry);
    }
  }

  /**
   * Check if the entry is new or diff and add as necessary.
   */
  private void addToDocument(AutoTuneCollection.Entry entry) {

    ObjectGraphOrigin point = entry.getOrigin();
    OrmQueryDetail profileDetail = entry.getDetail();

    // compare with the existing query tuning entry
    OrmQueryDetail tuneDetail = queryTuner.get(point.key());
    if (tuneDetail == null) {
      addToDocumentNewEntry(entry, point);

    } else if (!tuneDetail.isAutoTuneEqual(profileDetail)) {
      addToDocumentDiffEntry(entry, point, tuneDetail);
    }
  }

  /**
   * Add as a diff entry.
   */
  private void addToDocumentDiffEntry(AutoTuneCollection.Entry entry, ObjectGraphOrigin point, OrmQueryDetail tuneDetail) {

    diffCount++;

    Origin origin = createOrigin(entry, point, tuneDetail.asString());
    ProfileDiff diff = document.getProfileDiff();
    if (diff == null) {
      diff = new ProfileDiff();
      document.setProfileDiff(diff);
    }
    diff.getOrigin().add(origin);
  }

  /**
   * Add as a "new" entry.
   */
  private void addToDocumentNewEntry(AutoTuneCollection.Entry entry, ObjectGraphOrigin point) {

    newCount++;

    ProfileNew profileNew = document.getProfileNew();
    if (profileNew == null) {
      profileNew = new ProfileNew();
      document.setProfileNew(profileNew);
    }
    Origin origin = createOrigin(entry, point, entry.getOriginalQuery());
    profileNew.getOrigin().add(origin);
  }


  /**
   * Create the XML Origin bean for the given entry and ObjectGraphOrigin.
   */
  private Origin createOrigin(AutoTuneCollection.Entry entry, ObjectGraphOrigin point, String query) {

    Origin origin = new Origin();
    origin.setKey(point.key());
    origin.setBeanType(point.beanType());
    origin.setDetail(entry.getDetail().asString());
    origin.setCallStack(point.callOrigin().description());
    origin.setOriginal(query);

    if (updateTuning) {
      queryTuner.put(origin);
    }

    return origin;
  }

}
