package io.ebeaninternal.server.deploy;

import java.util.Set;

/**
 * Converts logical property names to database columns.
 */
public abstract class DeployParser {

  /**
   * used to identify sql literal.
   */
  private static final char SINGLE_QUOTE = '\'';

  /**
   * used to identify query named parameters.
   */
  private static final char COLON = ':';

  /**
   * Used to determine when a column name terminates.
   */
  private static final char UNDERSCORE = '_';

  private static final char OPEN_SQUARE_BRACKET = '[';
  private static final char CLOSE_SQUARE_BRACKET = ']';
  private static final char DOUBLE_QUOTE = '\"';
  private static final char BACK_QUOTE = '`';

  /**
   * Used to determine when a column name terminates.
   */
  private static final char PERIOD = '.';

  private static final char OPEN_BRACKET = '(';

  boolean encrypted;
  private String source;
  private StringBuilder sb;
  private int sourceLength;
  private int pos;
  String priorWord;
  String word;
  private char wordTerminator;
  private StringBuilder wordBuffer;

  protected abstract String convertWord();

  public abstract String getDeployWord(String expression);

  /**
   * Return the join includes.
   */
  public abstract Set<String> getIncludes();

  public void setEncrypted(boolean encrypted) {
    this.encrypted = encrypted;
  }

  public String parse(String source) {
    if (source == null) {
      return null;
    }
    pos = -1;
    this.source = source;
    this.sourceLength = source.length();
    this.sb = new StringBuilder(source.length() + 20);
    while (nextWord()) {
      if (skipWordConvert()) {
        sb.append(word);
        priorWord = word;
      } else {
        String deployWord = convertWord();
        sb.append(deployWord);
        priorWord = deployWord;
      }
      if (pos < sourceLength) {
        if (wordTerminator != OPEN_BRACKET) {
          sb.append(wordTerminator);
        }
        if (wordTerminator == SINGLE_QUOTE) {
          readLiteral();
        }
      }
    }
    return sb.toString();
  }

  boolean skipWordConvert() {
    return false;
  }

  private boolean nextWord() {
    if (!findWordStart()) {
      return false;
    }
    wordBuffer = new StringBuilder();
    wordBuffer.append(source.charAt(pos));
    while (++pos < sourceLength) {
      char ch = source.charAt(pos);
      if (isWordPart(ch)) {
        wordBuffer.append(ch);
      } else {
        wordTerminator = ch;
        break;
      }
    }
    word = wordBuffer.toString();
    return true;
  }

  private boolean findWordStart() {
    while (++pos < sourceLength) {
      char ch = source.charAt(pos);
      if (ch == SINGLE_QUOTE) {
        // read a literal value and just
        // append to string builder
        sb.append(ch);
        readLiteral();
      } else if (ch == COLON) {
        // read a named parameter
        // just append to string builder
        sb.append(ch);
        readNamedParameter();
      } else if (isWordStart(ch)) {
        // its the start of a word that could
        // be translated
        return true;
      } else {
        sb.append(ch);
      }
    }
    return false;
  }

  /**
   * Read the rest of a literal value. These do not get translated so are just
   * read and appended to the string builder.
   */
  private void readLiteral() {
    while (++pos < sourceLength) {
      char ch = source.charAt(pos);
      sb.append(ch);
      if (ch == SINGLE_QUOTE) {
        break;
      }
    }
  }

  /**
   * Read a named parameter. These are not translated. They will be replaced
   * by positioned parameters later.
   */
  private void readNamedParameter() {
    while (++pos < sourceLength) {
      char ch = source.charAt(pos);
      sb.append(ch);
      if (Character.isWhitespace(ch)) {
        break;
      } else if (ch == ',') {
        break;
      }
    }
  }

  /**
   * return true if the char is a letter, digit or underscore.
   */
  private boolean isWordPart(char ch) {
    if (ch == OPEN_BRACKET) {
      // include in the 'word' such that "count(" formula doesn't clash with property "count"
      wordBuffer.append(ch);
      return false;
    }
    return Character.isLetterOrDigit(ch) || ch == UNDERSCORE || ch == PERIOD || ch == DOUBLE_QUOTE || ch == CLOSE_SQUARE_BRACKET || ch == BACK_QUOTE;
  }

  private boolean isWordStart(char ch) {
    return Character.isLetter(ch) || ch == UNDERSCORE || ch == DOUBLE_QUOTE || ch == OPEN_SQUARE_BRACKET || ch == BACK_QUOTE;
  }
}
