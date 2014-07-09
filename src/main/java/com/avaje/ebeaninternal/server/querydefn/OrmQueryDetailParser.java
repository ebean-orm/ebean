package com.avaje.ebeaninternal.server.querydefn;

import javax.persistence.PersistenceException;

/**
 * Parses a Object relational query statement into a OrmQueryDetail and OrmQueryAttributes.
 * <p>
 * The reason they are split into detail and attributes is that the autoFetch feature is used to
 * replace the OrmQueryDetail leaving the attributes unchanged.
 * </p>
 */
public class OrmQueryDetailParser {

  private final OrmQueryDetail detail = new OrmQueryDetail();

  private int maxRows;

  private int firstRow;

  private String rawWhereClause;

  private String rawOrderBy;

  private final SimpleTextParser parser;

  public OrmQueryDetailParser(String oql) {
    this.parser = new SimpleTextParser(oql);
  }

  public void parse() throws PersistenceException {

    parser.nextWord();
    processInitial();
  }

  protected void assign(DefaultOrmQuery<?> query) {
    query.setOrmQueryDetail(detail);
    query.setFirstRow(firstRow);
    query.setMaxRows(maxRows);
    query.setRawWhereClause(rawWhereClause);
    query.order(rawOrderBy);
  }

  private void processInitial() {
    if (parser.isMatch("find")) {
      OrmQueryProperties props = readFindFetch();
      detail.setBase(props);
    } else {
      process();
    }
    while (!parser.isFinished()) {
      process();
    }
  }

  private boolean isFetch() {
    return parser.isMatch("fetch") || parser.isMatch("join");
  }

  private void process() {
    if (isFetch()) {
      OrmQueryProperties props = readFindFetch();
      detail.putFetchPath(props);

    } else if (parser.isMatch("where")) {
      readWhere();

    } else if (parser.isMatch("order", "by")) {
      readOrderBy();

    } else if (parser.isMatch("limit")) {
      readLimit();

    } else {
      throw new PersistenceException("Query expected 'fetch', 'where','order by' or 'limit' keyword but got ["
          + parser.getWord() + "] \r " + parser.getOql());
    }
  }

  private void readLimit() {
    try {
      String maxLimit = parser.nextWord();
      maxRows = Integer.parseInt(maxLimit);

      String offsetKeyword = parser.nextWord();
      if (offsetKeyword != null) {
        if (!parser.isMatch("offset")) {
          throw new PersistenceException("expected offset keyword but got " + parser.getWord());
        }
        String firstRowLimit = parser.nextWord();
        firstRow = Integer.parseInt(firstRowLimit);
        parser.nextWord();
      }
    } catch (NumberFormatException e) {
      String msg = "Expected an integer for maxRows or firstRows in limit offset clause";
      throw new PersistenceException(msg, e);
    }
  }

  private void readOrderBy() {
    // read the by
    parser.nextWord();

    StringBuilder sb = new StringBuilder();
    while (parser.nextWord() != null) {
      if (parser.isMatch("limit")) {
        break;
      } else {
        String w = parser.getWord();
        if (!w.startsWith("(")) {
          sb.append(" ");
        }
        sb.append(w);
      }
    }
    rawOrderBy = sb.toString().trim();

    if (!parser.isFinished()) {
      readLimit();
    }
  }

  private void readWhere() {

    int nextMode = 0;
    StringBuilder sb = new StringBuilder();
    while ((parser.nextWord()) != null) {
      if (parser.isMatch("order", "by")) {
        nextMode = 1;
        break;

      } else if (parser.isMatch("limit")) {
        nextMode = 2;
        break;

      } else {
        sb.append(" ").append(parser.getWord());
      }
    }
    String whereClause = sb.toString().trim();
    if (whereClause.length() > 0) {
      rawWhereClause = whereClause;
    }

    if (nextMode == 1) {
      readOrderBy();
    } else if (nextMode == 2) {
      readLimit();
    }
  }

  private OrmQueryProperties readFindFetch() {

    boolean readAlias = false;

    String props = null;
    String path = parser.nextWord();
    String token = null;
    while ((token = parser.nextWord()) != null) {
      if (!readAlias && parser.isMatch("as")) {
        // next token is alias
        parser.nextWord();
        readAlias = true;

      } else if ('(' == token.charAt(0)) {
        props = token;
        parser.nextWord();
        break;

      } else if (isFindFetchEnd()) {
        break;

      } else if (!readAlias) {
        readAlias = true;

      } else {
        throw new PersistenceException("Expected (props) or new 'fetch' 'where' but got " + token);
      }
    }
    if (props != null) {
      props = props.substring(1, props.length() - 1);
    }
    return new OrmQueryProperties(path, props);
  }

  private boolean isFindFetchEnd() {
    if (isFetch()) {
      return true;
    }
    if (parser.isMatch("where")) {
      return true;
    }
    if (parser.isMatch("order", "by")) {
      return true;
    }
    return false;
  }
}
