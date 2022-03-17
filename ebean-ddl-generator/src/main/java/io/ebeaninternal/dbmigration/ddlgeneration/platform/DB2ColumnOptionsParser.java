package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parses the inline column options of a create-statement (see
 * https://www.ibm.com/docs/en/db2/11.5?topic=statements-create-table#sdx-synid_frag-column-options) into separate pieces if you
 * want to alter a column. The later one has to be done in separate statements: See
 * https://www.ibm.com/docs/en/db2/11.5?topic=statements-alter-table#sdx-synid_frag-column-options
 * 
 * <br>
 * Note 1: This class parses only 'inline length', 'compact' and 'logged' options<br>
 * Note 2: You cannot alter the compact or logged option on an existing column.
 * 
 * @author Roland Praml, FOCONIS AG
 */
public class DB2ColumnOptionsParser {
  private static final Pattern INLINE_LENGTH = Pattern.compile(".*( inline length \\d+).*", Pattern.CASE_INSENSITIVE);
  private static final Pattern NOT_LOGGED = Pattern.compile(".*?(( not)? logged).*", Pattern.CASE_INSENSITIVE);
  private static final Pattern NOT_COMPACT = Pattern.compile(".*?(( not)? compact).*", Pattern.CASE_INSENSITIVE);

  private final String type;
  private final String inlineLength;
  private final boolean logged;
  private final boolean compact;
  private boolean extraOptions;

  DB2ColumnOptionsParser(String def) {
    Matcher m = INLINE_LENGTH.matcher(def);
    if (m.matches()) {
      def = def.replace(m.group(1), "");
      inlineLength = m.group(1).trim();
    } else {
      inlineLength = null;
    }
    m = NOT_LOGGED.matcher(def);
    if (m.matches()) {
      extraOptions = true;
      def = def.replace(m.group(1), "");
      logged = m.group(2) == null;
    } else {
      logged = true; // default
    }

    m = NOT_COMPACT.matcher(def);
    if (m.matches()) {
      extraOptions = true;
      def = def.replace(m.group(1), "");
      compact = m.group(2) == null;
    } else {
      compact = false; // default
    }
    type = def.trim();
  }

  public String getInlineLength() {
    return inlineLength;
  }

  public String getType() {
    return type;
  }

  public boolean isLogged() {
    return logged;
  }

  public boolean isCompact() {
    return compact;
  }

  public boolean hasExtraOptions() {
    return extraOptions;
  }

}
