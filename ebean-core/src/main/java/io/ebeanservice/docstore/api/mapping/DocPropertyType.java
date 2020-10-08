package io.ebeanservice.docstore.api.mapping;

/**
 * Types as defined for document store property types.
 */
public enum DocPropertyType {

  /**
   * Enum.
   */
  ENUM,

  /**
   * A UUID is a String Id implying it should not be analysed.
   */
  UUID,

  /**
   * Keyword/code string content not expected to be analysed.
   */
  KEYWORD,

  /**
   * String content expected to be analysed.
   */
  TEXT,

  /**
   * Boolean.
   */
  BOOLEAN,

  /**
   * Short.
   */
  SHORT,

  /**
   * Integer.
   */
  INTEGER,

  /**
   * Long.
   */
  LONG,

  /**
   * Float.
   */
  FLOAT,

  /**
   * Double.
   */
  DOUBLE,

  /**
   * Date without time.
   */
  DATE,

  /**
   * Date with time.
   */
  DATETIME,

  /**
   * Binary type.
   */
  BINARY,

  /**
   * A nested object.
   */
  OBJECT,

  /**
   * A nested list of objects.
   */
  LIST,

  /**
   * Root level type.
   */
  ROOT

}
