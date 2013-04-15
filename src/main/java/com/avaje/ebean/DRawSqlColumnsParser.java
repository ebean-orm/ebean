package com.avaje.ebean;

import java.util.ArrayList;
import java.util.Arrays;

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

    if (split.length == 1) {
      // default to column the same name as the property
      return new ColumnMapping.Column(indexPos++, split[0], null);
    }
    if (split.length == 2) {
      return new ColumnMapping.Column(indexPos++, split[0], split[1]);
    }
    if (split.length == 3) {
      if (!split[1].equalsIgnoreCase("as")) {
        String msg = "Expecting AS keyword parsing column " + colInfo;
        throw new PersistenceException(msg);
      }
      return new ColumnMapping.Column(indexPos++, split[0], split[2]);
    }

    String msg = "Expecting Max 3 words parsing column " + colInfo + ". Got "
        + Arrays.toString(split);
    throw new PersistenceException(msg);
  }

  private int nextComma() {
    boolean inQuote = false;
    while (pos < end) {
      char c = sqlSelect.charAt(pos);
      if (c == '\'') {
        inQuote = !inQuote;
      } else if (!inQuote && c == ',') {
        return pos;
      }
      pos++;
    }
    return pos;
  }
}
