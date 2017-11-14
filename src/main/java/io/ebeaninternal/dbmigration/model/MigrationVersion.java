package io.ebeaninternal.dbmigration.model;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * The version of a migration used so that migrations are processed in order.
 */
public class MigrationVersion implements Comparable<MigrationVersion> {

  private static final Pattern SECTION_SPLITTER = Pattern.compile("[\\.-]");

  private static final int[] REPEAT_ORDERING = {Integer.MAX_VALUE};

  private static final boolean[] REPEAT_UNDERSCORES = {false};

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

  /**
   * Construct for "repeatable" version.
   */
  private MigrationVersion(String raw, String comment) {
    this.raw = raw;
    this.comment = comment;
    this.ordering = REPEAT_ORDERING;
    this.underscores = REPEAT_UNDERSCORES;
  }

  /**
   * Construct for "normal" version.
   */
  private MigrationVersion(String raw, int[] ordering, boolean[] underscores, String comment) {
    this.raw = raw;
    this.ordering = ordering;
    this.underscores = underscores;
    this.comment = comment;
  }

  /**
   * Return true if this is a "repeatable" version.
   */
  public boolean isRepeatable() {
    // Clarification: The comparison here is intended to compare object references and not the content of the arrays.
    // This is a kind of shortcut to see if some configuration is in place that results in the use of something other than the default.
    return ordering == REPEAT_ORDERING;
  }

  /**
   * Return the full version.
   */
  public String getFull() {
    return raw;
  }

  @Override
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
   * <p>
   * Normalised means always use '.' delimiters (no underscores).
   * NextVersion means bump/increase the last version number by 1.
   */
  private String formattedVersion(boolean normalised, boolean nextVersion) {

    // Clarification: The comparison here is intended to compare object references and not the content of the arrays.
    // This is a kind of shortcut to see if some configuration is in place that results in the use of something other than the default.
    if (ordering == REPEAT_ORDERING) {
      return "R";
    }
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
    return comment.compareTo(other.comment);
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

    if (raw.startsWith("V") || raw.startsWith("v")) {
      raw = raw.substring(1);
    }

    String comment = "";
    String value = raw;
    int commentStart = raw.indexOf("__");
    if (commentStart > -1) {
      // trim off the trailing comment
      comment = raw.substring(commentStart + 2);
      value = value.substring(0, commentStart);
    }

    value = value.replace('_', '.');

    String[] sections = SECTION_SPLITTER.split(value);

    if ("r".equalsIgnoreCase(sections[0])) {
      // a "repeatable" version (does not have a version number)
      return new MigrationVersion(raw, comment);
    }

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
