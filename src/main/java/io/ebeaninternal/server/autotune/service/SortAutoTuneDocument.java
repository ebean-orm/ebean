package io.ebeaninternal.server.autotune.service;

import io.ebeaninternal.server.autotune.model.Autotune;
import io.ebeaninternal.server.autotune.model.Origin;
import io.ebeaninternal.server.autotune.model.ProfileDiff;
import io.ebeaninternal.server.autotune.model.ProfileEmpty;
import io.ebeaninternal.server.autotune.model.ProfileNew;

import java.util.Comparator;
import java.util.List;

/**
 * Sorts Autotune document by
 */
public class SortAutoTuneDocument {


  /**
   * Set the diff and new entries by bean type followed by key.
   */
  public static void sort(Autotune document) {

    ProfileDiff profileDiff = document.getProfileDiff();
    if (profileDiff != null) {
      profileDiff.getOrigin().sort(NAME_KEY_SORT);
    }
    ProfileNew profileNew = document.getProfileNew();
    if (profileNew != null) {
      profileNew.getOrigin().sort(NAME_KEY_SORT);
    }
    ProfileEmpty profileEmpty = document.getProfileEmpty();
    if (profileEmpty != null) {
      profileEmpty.getOrigin().sort(KEY_SORT);
    }
    List<Origin> origins = document.getOrigin();
    if (!origins.isEmpty()) {
      origins.sort(NAME_KEY_SORT);
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
