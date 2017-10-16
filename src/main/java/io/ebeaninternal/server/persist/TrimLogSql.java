package io.ebeaninternal.server.persist;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to improve logging of raw SQL that contains new line characters.
 */
public class TrimLogSql {

  /**
   * Statically compiled Regex, to avoid having it be compiled every time it is used.
   */
  private static final Pattern LINUX_NEW_LINE_REPLACE_PATTERN = Pattern.compile("\n", Pattern.LITERAL);

  /**
   * Replace new line chars for nicer logging of multi-line sql strings.
   */
  public static String trim(String sql) {
    return LINUX_NEW_LINE_REPLACE_PATTERN.matcher(sql).replaceAll(Matcher.quoteReplacement("\\n "));
  }
}
