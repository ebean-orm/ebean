package io.ebeaninternal.server.core.bootup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * Distill packages into distinct top level packages for searching.
 */
class DistillPackages {


  /**
   * Distill the list of packages into distinct top level packages.
   */
  static List<String> distill(Collection<String> packages, Collection<String> mfPackages) {

    // sort into natural order
    TreeSet<String> treeSet = new TreeSet<>();
    treeSet.addAll(packages);
    treeSet.addAll(mfPackages);

    List<String> distilled = new ArrayList<>();

    // build the distilled list
    for (String pack : treeSet) {
      if (notAlreadyContained(distilled, pack)) {
        distilled.add(pack);
      }
    }

    return distilled;
  }

  /**
   * Return true if the package is not already contained in the distilled list.
   */
  private static boolean notAlreadyContained(List<String> distilled, String pack) {

    for (String aDistilled : distilled) {
      if (pack.startsWith(aDistilled)) {
        return false;
      }
    }
    return true;
  }
}
