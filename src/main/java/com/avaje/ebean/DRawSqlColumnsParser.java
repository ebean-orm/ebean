package com.avaje.ebean;

import java.util.ArrayList;

import javax.persistence.PersistenceException;

import com.avaje.ebean.RawSql.ColumnMapping;

/**
 * Parses columnMapping (select clause) mapping columns to bean properties.
 */
final class DRawSqlColumnsParser {

  private final int end;

  private final String sqlSelect;

  private int pos;

  private int indexPos;

  public static ColumnMapping parse(String sqlSelect) {
    return new DRawSqlColumnsParser(sqlSelect).parse();
  }

  private DRawSqlColumnsParser(String sqlSelect) {
    this.sqlSelect = sqlSelect;
    this.end = sqlSelect.length();
  }

  private ColumnMapping parse() {

    ArrayList<ColumnMapping.Column> columns = new ArrayList<ColumnMapping.Column>();
    while (pos <= end) {
      ColumnMapping.Column c = nextColumnInfo();
      columns.add(c);
    }

    return new ColumnMapping(columns);
  }

  private ColumnMapping.Column nextColumnInfo() {
    int start = pos;
    nextComma();
    String colInfo = sqlSelect.substring(start, pos++);
    colInfo = colInfo.trim();

    String[] split = colInfo.split(" ");
    if (split.length > 1) {
      ArrayList<String> tmp = new ArrayList<String>(split.length);
      for (int i = 0; i < split.length; i++) {
        if (split[i].trim().length() > 0) {
          tmp.add(split[i].trim());
        }
      }
      split = tmp.toArray(new String[tmp.size()]);
    }

    if (split.length == 0) {
      throw new PersistenceException("Huh? Not expecting length=0 when parsing column " + colInfo);
    }
    if (split.length == 1) {
      // default to column the same name as the property
      return new ColumnMapping.Column(indexPos++, split[0], null);
    }
    if (split.length == 2) {
      return new ColumnMapping.Column(indexPos++, split[0], split[1]);
    }
    // Ok, we now expect/require the AS keyword and it should be the 
    // second to last word in the colInfo content 
    if (!split[split.length - 2].equalsIgnoreCase("as")) {
      throw new PersistenceException("Expecting AS keyword as second to last word when parsing column " + colInfo);
    }
    // build back the 'column formula' that precedes the AS keyword
    StringBuilder sb = new StringBuilder();
    sb.append(split[0]);
    for (int i = 1; i < split.length-2; i++) {
      sb.append(" ").append(split[i]);
    }
    return new ColumnMapping.Column(indexPos++, sb.toString(), split[split.length - 1]);
  }

  private void nextComma() {
    boolean inQuote = false;
    while (pos < end) {
      char c = sqlSelect.charAt(pos);
      if (c == '\'') {
        inQuote = !inQuote;
      } else if (!inQuote && c == ',') {
        return;
      }
      pos++;
    }
  }
}
