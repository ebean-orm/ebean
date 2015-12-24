package com.avaje.ebean.dbmigration.model;

/**
 * The version of a migration used so that migrations are processed in order.
 */
public class MigrationVersion implements Comparable<MigrationVersion> {

  /**
   * The raw version text.
   */
  private final String raw;

  /**
   * The ordering parts.
   */
  private final int[] ordering;

  private MigrationVersion(String raw, int[] ordering) {
    this.raw = raw;
    this.ordering = ordering;
  }

  public String toString() {
    return raw;
  }


  public String nextVersion() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ordering.length; i++) {
      if (i < ordering.length -1 ) {
        sb.append(ordering[i]).append(".");
      } else {
        sb.append(ordering[i]+1);
      }
    }
    return sb.toString();
  }

  @Override
  public int compareTo(MigrationVersion other) {

    int otherLength = other.ordering.length;
    for (int i = 0; i < ordering.length; i++) {
      if (i >= otherLength) {
        // considered greater
        return 1;
      }
      if (ordering[i] != other.ordering[i]) {
        return (ordering[i] > other.ordering[i]) ? 1 : -1;
      }
    }
    // considered the same
    return 0;
  }

  /**
   * Parse the raw version string into a MigrationVersion.
   */
  public static MigrationVersion parse(String raw) {

    String value = raw.replace("__",".");
    value = value.replace('_','.');

    String[] sections = value.split("\\.");

    int[] ordering = new int[sections.length];

    int stopIndex = 0;
    for (int i = 0; i < sections.length; i++) {
      try {
        ordering[i] = Integer.parseInt(sections[i]);
        stopIndex++;
      } catch (NumberFormatException e) {
        // stop parsing
        break;
      }
    }

    int[] actualOrder = new int[stopIndex];
    System.arraycopy(ordering, 0, actualOrder, 0, stopIndex);

    return new MigrationVersion(raw, actualOrder);
  }

}
