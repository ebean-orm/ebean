package io.ebeaninternal.server.rawsql;

import io.ebeaninternal.server.rawsql.SpiRawSql.ColumnMapping;
import io.ebeaninternal.server.util.DSelectColumnsParser;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parses columnMapping (select clause) mapping columns to bean properties.
 */
final class DRawSqlColumnsParser {

  private static final Pattern COLINFO_SPLIT = Pattern.compile("\\s(?=[^\\)]*(?:\\(|$))");

  private final String sqlSelect;

  private int indexPos;

  public static ColumnMapping parse(String sqlSelect) {
    return new DRawSqlColumnsParser(sqlSelect).parse();
  }

  private DRawSqlColumnsParser(String sqlSelect) {
    this.sqlSelect = sqlSelect;
  }

  private ColumnMapping parse() {

    List<String> columnList = DSelectColumnsParser.parse(sqlSelect);

    List<ColumnMapping.Column> columns = new ArrayList<>(columnList.size());

    for (String rawColumn : columnList) {
      columns.add(parseColumn(rawColumn));
    }
    return new ColumnMapping(columns);
  }

  private ColumnMapping.Column parseColumn(String colInfo) {

    String[] split = COLINFO_SPLIT.split(colInfo);
    if (split.length > 1) {
      ArrayList<String> tmp = new ArrayList<>(split.length);
      for (String aSplit : split) {
        if (!aSplit.trim().isEmpty()) {
          tmp.add(aSplit.trim());
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
    for (int i = 1; i < split.length - 2; i++) {
      sb.append(" ").append(split[i]);
    }
    return new ColumnMapping.Column(indexPos++, sb.toString(), split[split.length - 1]);
  }

}
