package io.ebeaninternal.server.expression;

import io.ebean.annotation.Platform;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Adds values as literals to SQL string.
 */
interface InLiterals {

  /**
   * Add the value as a SQL literal to the buffer.
   */
  void append(StringBuilder buffer, Object value);

  /**
   * Return the InLiterals for the type of the given value.
   */
  static InLiterals of(Object val, Platform platform) {
    if (val instanceof Number) {
      return NumLiteral.INSTANCE;
    }
    if (val instanceof String || val instanceof UUID) {
      return StrLiteral.INSTANCE;
    }
    if (val instanceof LocalDate) {
      return platform.base() == Platform.MYSQL ? DateEscapeLiteral.INSTANCE : DateLiteral.INSTANCE;
    }
    throw new UnsupportedOperationException();
  }

  final class NumLiteral implements InLiterals {

    static NumLiteral INSTANCE = new NumLiteral();

    @Override
    public void append(StringBuilder buffer, Object value) {
      buffer.append(value);
    }
  }

  final class StrLiteral implements InLiterals {

    static StrLiteral INSTANCE = new StrLiteral();

    @Override
    public void append(StringBuilder buffer, Object value) {
      buffer.append('\'').append(value).append('\'');
    }
  }

  final class DateLiteral implements InLiterals {

    static DateLiteral INSTANCE = new DateLiteral();

    @Override
    public void append(StringBuilder buffer, Object value) {
      buffer.append("date ").append('\'').append(value).append('\'');
    }
  }

  final class DateEscapeLiteral implements InLiterals {

    static DateEscapeLiteral INSTANCE = new DateEscapeLiteral();

    @Override
    public void append(StringBuilder buffer, Object value) {
      buffer.append("{d '").append(value).append("'}");
    }
  }
}
