package com.avaje.ebean.dbmigration.model;

import java.util.Arrays;

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

  private final boolean[] underscores;

  private final String comment;

  private MigrationVersion(String raw, int[] ordering, boolean[] underscores, String comment) {
    this.raw = raw;
    this.ordering = ordering;
    this.underscores = underscores;
    this.comment = comment;
  }

  public String toString() {
    return raw;
  }

  /**
   * Return the version comment.
   */
  public String getComment() {
    return comment;
  }

  /**
   * Return the version in raw form.
   */
  public String getRaw() {
    return raw;
  }

  /**
   * Return the trimmed version excluding version comment and un-parsable string.
   */
  public String asString() {
    return formattedVersion(false, false);
  }

  /**
   * Return the trimmed version with any underscores replaced with '.'
   */
  public String normalised() {
    return formattedVersion(true, false);
  }

  /**
   * Return the next version based on this version.
   */
  public String nextVersion() {
    return formattedVersion(false, true);
  }

  /**
   * Returns the version part of the string.
   *
   * Normalised means always use '.' delimiters (no underscores).
   * NextVersion means bump/increase the last version number by 1.
   */
  private String formattedVersion(boolean normalised, boolean nextVersion) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ordering.length; i++) {
      if (i < ordering.length - 1) {
        sb.append(ordering[i]);
        if (normalised) {
          sb.append('.');
        } else {
          sb.append(underscores[i] ? '_' : '.');
        }
      } else {
        sb.append((nextVersion) ? ordering[i] + 1 : ordering[i]);
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
   * Parse the raw version string and just return the leading version number;
   */
  public static String trim(String raw) {
    return parse(raw).asString();
  }

  /**
   * Parse the raw version string into a MigrationVersion.
   */
  public static MigrationVersion parse(String raw) {

    String comment = "";
    String value = raw;
    int commentStart = raw.indexOf("__");
    if (commentStart > -1) {
      // trim off the trailing comment
      comment = raw.substring(commentStart + 2);
      value = value.substring(0, commentStart);
    }

    value = value.replace('_', '.');

    String[] sections = value.split("\\.");

    boolean[] underscores = new boolean[sections.length];
    int[] ordering = new int[sections.length];

    int delimiterPos = 0;
    int stopIndex = 0;
    for (int i = 0; i < sections.length; i++) {
      try {
        ordering[i] = Integer.parseInt(sections[i]);
        stopIndex++;

        delimiterPos += sections[i].length();
        underscores[i] = (delimiterPos < raw.length() - 1 && raw.charAt(delimiterPos) == '_');
        delimiterPos++;
      } catch (NumberFormatException e) {
        // stop parsing
        break;
      }
    }

    int[] actualOrder = Arrays.copyOf(ordering, stopIndex);
    boolean[] actualUnderscores = Arrays.copyOf(underscores, stopIndex);

    return new MigrationVersion(raw, actualOrder, actualUnderscores, comment);
  }

}
