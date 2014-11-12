package com.avaje.ebean;

class SimpleTextParser {

  private final String oql;
  private final char[] chars;
  private final int eof;

  private int pos;
  private String word;
  private String lowerWord;

  SimpleTextParser(String oql) {
    this.oql = oql;
    this.chars = oql.toCharArray();
    this.eof = oql.length();
  }

  public int getPos() {
    return pos;
  }

  public String getWord() {
    return word;
  }

  public String peekNextWord() {
    int origPos = pos;
    String nw = nextWordInternal();
    pos = origPos;
    return nw;
  }

  /**
   * Match the current and the next word.
   */
  public boolean isMatch(String lowerMatch, String nextWordMatch) {

    if (isMatch(lowerMatch)) {
      String nw = peekNextWord();
      if (nw != null) {
        nw = nw.toLowerCase();
        return nw.equals(nextWordMatch);
      }
    }
    return false;
  }

  public boolean isFinished() {
    return word == null;
  }

  public int findWordLower(String lowerMatch, int afterPos) {
    this.pos = afterPos;
    return findWordLower(lowerMatch);
  }

  public int findWordLower(String lowerMatch) {
    do {
      if (nextWord() == null) {
        return -1;
      }
      if (lowerMatch.equals(lowerWord)) {
        return pos - lowerWord.length();
      }
    } while (true);
  }

  /**
   * Match the current word.
   */
  public boolean isMatch(String lowerMatch) {
    return lowerMatch.equals(lowerWord);
  }

  public String nextWord() {
    word = nextWordInternal();
    if (word != null) {
      lowerWord = word.toLowerCase();
    }
    return word;
  }

  private String nextWordInternal() {
    trimLeadingWhitespace();
    if (pos >= eof) {
      return null;
    }
    int start = pos;
    if (chars[pos] == '(') {
      moveToClose();
    } else {
      moveToEndOfWord();
    }
    return oql.substring(start, pos);
  }

  private void moveToClose() {

    pos++;
    int openParenthesisCount = 0;

    for (; pos < eof; pos++) {
      char c = chars[pos];
      if (c == '(') {
        // count nested parenthesis
        openParenthesisCount++;

      } else if (c == ')') {
        if (openParenthesisCount > 0) {
          // still in nested parenthesis
          --openParenthesisCount;
        } else {
          // we have found the end
          pos++;
          return;
        }
      }
    }
  }

  private void moveToEndOfWord() {
    char c = chars[pos];
    boolean isOperator = isOperator(c);
    for (; pos < eof; pos++) {
      c = chars[pos];
      if (isWordTerminator(c, isOperator)) {
        return;
      }
    }
  }

  private boolean isWordTerminator(char c, boolean isOperator) {
    if (Character.isWhitespace(c)) {
      return true;
    }
    if (isOperator(c)) {
      return !isOperator;
    }
    return c == '(' || isOperator;
  }

  private boolean isOperator(char c) {
    switch (c) {
    case '<':
      return true;
    case '>':
      return true;
    case '=':
      return true;
    case '!':
      return true;

    default:
      return false;
    }
  }

  private void trimLeadingWhitespace() {
    for (; pos < eof; pos++) {
      char c = chars[pos];
      if (!Character.isWhitespace(c)) {
        break;
      }
    }
  }
}