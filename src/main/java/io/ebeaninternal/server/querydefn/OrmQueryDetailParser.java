package io.ebeaninternal.server.querydefn;

import javax.persistence.PersistenceException;

/**
 * Parses a Object relational query statement into a OrmQueryDetail and OrmQueryAttributes.
 * <p>
 * The reason they are split into detail and attributes is that the AutoTune feature is used to
 * replace the OrmQueryDetail leaving the attributes unchanged.
 * </p>
 */
public class OrmQueryDetailParser {

  private final OrmQueryDetail detail = new OrmQueryDetail();

  private final SimpleTextParser parser;

  public OrmQueryDetailParser(String oql) {
    this.parser = new SimpleTextParser(oql);
  }

  public OrmQueryDetail parse() throws PersistenceException {

    if (parser.isEmpty()) return detail;

    parser.nextWord();
    processInitial();
    return detail;
  }

  private void processInitial() {
    if (parser.isMatch("select")) {
      readSelect();
    } else if (parser.isMatch("find")) {
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
      detail.fetch(readFindFetch());
    } else {
      throw new PersistenceException("Query expected 'fetch', 'where','order by' or 'limit' keyword but got ["
        + parser.getWord() + "] \r " + parser.getOql());
    }
  }

  private void readSelect() {
    String props = parser.nextWord();
    if (props.startsWith("(")) {
      props = props.substring(1, props.length() - 1);
      OrmQueryProperties base = new OrmQueryProperties(null, props);
      detail.setBase(base);
      parser.nextWord();
    } else {
      process();
    }
  }

  private OrmQueryProperties readFindFetch() {

    boolean readAlias = false;

    String props = null;
    String path = parser.nextWord();
    String token;
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
    return isFetch() || parser.isMatch("where") || parser.isMatch("order", "by");
  }
}
