package com.avaje.ebeaninternal.server.autotune.service;

import com.avaje.ebean.bean.ObjectGraphOrigin;
import com.avaje.ebeaninternal.server.autotune.AutoTuneCollection;
import com.avaje.ebeaninternal.server.autotune.model.Autotune;
import com.avaje.ebeaninternal.server.autotune.model.Origin;
import com.avaje.ebeaninternal.server.autotune.model.ProfileDiff;
import com.avaje.ebeaninternal.server.autotune.model.ProfileEmpty;
import com.avaje.ebeaninternal.server.autotune.model.ProfileNew;
import com.avaje.ebeaninternal.server.querydefn.OrmQueryDetail;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Event where profiling information collected and processed.
 * <p>
 *
 * </p>
 */
public class ProfileCollectionEvent {

  //private static final Logger logger = LoggerFactory.getLogger(ProfileCollectionEvent.class);

  final Autotune document = new Autotune();

  final AutoTuneCollection profiling;

  final BaseQueryTuner queryTuner;

  final boolean emptyEntries;

  int newCount;

  int diffCount;

  public ProfileCollectionEvent(AutoTuneCollection profiling, BaseQueryTuner queryTuner, boolean emptyEntries) {
    this.profiling = profiling;
    this.queryTuner = queryTuner;
    this.emptyEntries = emptyEntries;
  }

  public boolean isEmpty() {
    return newCount == 0 && diffCount == 0;
  }

  public Autotune getDocument() {
    return document;
  }

  public int getDiffCount() {
    return diffCount;
  }

  public int getNewCount() {
    return newCount;
  }

  public void writeFile(String filePrefix) {

    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
    String now = df.format(new Date());

    // write the file with serverName and now suffix as we can output the profiling many times
    File file = new File(filePrefix + "-" + now + ".xml");
    AutoTuneXmlWriter writer = new AutoTuneXmlWriter();
    writer.write(document, file);
  }

  public boolean process() {


    Set<String> tunerKeys = (emptyEntries) ? queryTuner.keySet() : null;
    Set<String> profileKeys = (emptyEntries) ? new HashSet<String>() : null;

    List<AutoTuneCollection.Entry> profilingEntries = profiling.getEntries();

    for (AutoTuneCollection.Entry entry : profilingEntries) {
      addToDocument(entry);
      if (profileKeys != null) {
        // only collect if we are actively looking for tuning
        // entries that were not used
        profileKeys.add(entry.getOrigin().getKey());
      }
    }

    if (tunerKeys != null) {
      // report the origin keys that we didn't collect any profiling on
      for (String tuneKey : tunerKeys) {
        if (!profileKeys.contains(tuneKey)) {
          ProfileEmpty profileEmpty = document.getProfileEmpty();
          if (profileEmpty == null) {
            profileEmpty = new ProfileEmpty();
            document.setProfileEmpty(profileEmpty);
          }
          Origin emptyOrigin = new Origin();
          emptyOrigin.setKey(tuneKey);
          profileEmpty.getOrigin().add(emptyOrigin);
        }
      }
    }

    if (!isEmpty()) {
      sortDocument(document);
    }

    return isEmpty();
  }


  private void addToDocument(AutoTuneCollection.Entry entry) {

    ObjectGraphOrigin point = entry.getOrigin();
    OrmQueryDetail profileDetail = entry.getDetail();

    // compare with the existing query tuning entry
    OrmQueryDetail tuneDetail = queryTuner.get(point.getKey());
    if (tuneDetail == null) {
      Origin origin = addToDocumentNewEntry(entry, point);
      queryTuner.add(origin);

    } else if (!tuneDetail.isAutoTuneEqual(profileDetail)) {
      addToDocumentDiffEntry(entry, point, tuneDetail);
    }
  }

  private Origin addToDocumentDiffEntry(AutoTuneCollection.Entry entry, ObjectGraphOrigin point, OrmQueryDetail tuneDetail) {

    diffCount++;

    Origin origin = createOrigin(entry, point);
    origin.setOriginal(tuneDetail.toString());
    ProfileDiff diff = document.getProfileDiff();
    if (diff == null) {
      diff = new ProfileDiff();
      document.setProfileDiff(diff);
    }
    diff.getOrigin().add(origin);
    return origin;
  }

  private Origin addToDocumentNewEntry(AutoTuneCollection.Entry entry, ObjectGraphOrigin point) {

    newCount++;

    ProfileNew profileNew = document.getProfileNew();
    if (profileNew == null) {
      profileNew = new ProfileNew();
      document.setProfileNew(profileNew);
    }
    Origin origin = createOrigin(entry, point);
    origin.setOriginal(entry.getOriginalQuery());
    profileNew.getOrigin().add(origin);
    return origin;
  }


  /**
   * Create the XML Origin bean for the given entry and ObjectGraphOrigin.
   */
  private Origin createOrigin(AutoTuneCollection.Entry entry, ObjectGraphOrigin point) {
    Origin origin = new Origin();
    origin.setKey(point.getKey());
    origin.setBeanType(point.getBeanType());
    origin.setDetail(entry.getDetail().toString());
    origin.setCallStack(point.getCallStack().description("\n"));
    return origin;
  }

  /**
   * Set the diff and new entries by bean type followed by key.
   */
  private void sortDocument(Autotune document) {

    ProfileDiff profileDiff = document.getProfileDiff();
    if (profileDiff != null) {
      Collections.sort(profileDiff.getOrigin(), NAME_KEY_SORT);
    }
    ProfileNew profileNew = document.getProfileNew();
    if (profileNew != null) {
      Collections.sort(profileNew.getOrigin(), NAME_KEY_SORT);
    }
    ProfileEmpty profileEmpty = document.getProfileEmpty();
    if (profileEmpty != null) {
      Collections.sort(profileEmpty.getOrigin(), KEY_SORT);
    }
  }

  private static final OriginNameKeySort NAME_KEY_SORT = new OriginNameKeySort();

  private static final OriginKeySort KEY_SORT = new OriginKeySort();

  /**
   * Comparator sort by bean type then key.
   */
  private static class OriginNameKeySort implements Comparator<Origin> {

    @Override
    public int compare(Origin o1, Origin o2) {
      int comp = o1.getBeanType().compareTo(o2.getBeanType());
      if (comp == 0) {
        comp = o1.getKey().compareTo(o2.getKey());
      }
      return comp;
    }
  }

  /**
   * Comparator sort by bean type then key.
   */
  private static class OriginKeySort implements Comparator<Origin> {

    @Override
    public int compare(Origin o1, Origin o2) {
      return o1.getKey().compareTo(o2.getKey());
    }
  }
}
