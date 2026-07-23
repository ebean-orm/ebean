package io.ebean.core.type;

/**
 * Helper that removes whitespace from JSON content.
 * <p>
 * Used to normalise PostgreSQL JSONB content before it is retained for mutation detection.
 */
public final class JsonTrim {

  private JsonTrim() {
  }

  /**
   * Return JSON with whitespace trimmed.
   */
  public static String trim(String json) {
    if (json == null) {
      return null;
    }
    int len = json.length();
    StringBuilder builder = new StringBuilder(len);
    boolean escaped = false;
    boolean quoted = false;
    for (int i = 0; i < len; i++) {
      char c = json.charAt(i);
      if (c == '"') {
        if (!escaped) {
          quoted = !quoted;
        } else {
          escaped = false;
        }
      } else if (escaped) {
        escaped = false;
      } else if (quoted && c == '\\') {
        escaped = true;
      }
      if (quoted || !Character.isWhitespace(c)) {
        builder.append(c);
      }
    }
    return builder.toString();
  }
}
