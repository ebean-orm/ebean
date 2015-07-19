package com.avaje.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

/**
 * Wraps an underlying JsonGenerator taking into account null suppression and exposing isIncludeEmpty() etc.
 */
public interface JsonWriter {

  /**
   * Return the Jackson core JsonGenerator.
   */
  JsonGenerator gen();

  /**
   * Return true if null values should be included in JSON output.
   */
  boolean isIncludeNull();

  /**
   * Return true if empty collections should be included in the JSON output.
   */
  boolean isIncludeEmpty();

  /**
   * Write the field name.
   */
  void writeFieldName(String name) throws IOException;

  /**
   * Write a null value taking into account null value suppression.
   */
  void writeNullField(String name) throws IOException;

  /**
   * Write a number field.
   */
  void writeNumberField(String name, int value) throws IOException;

  /**
   * Write a number field.
   */
  void writeNumberField(String name, Short value) throws IOException;

  /**
   * Write a number field.
   */
  void writeNumberField(String name, Long value) throws IOException;

  /**
   * Write a number field.
   */
  void writeNumberField(String name, Double value) throws IOException;

  /**
   * Write a number field.
   */
  void writeNumberField(String name, Float value) throws IOException;

  /**
   * Write a number field.
   */
  void writeNumberField(String name, BigDecimal value) throws IOException;

  /**
   * Write a sting field.
   */
  void writeStringField(String name, String value) throws IOException;

  /**
   * Write a binary field.
   */
  void writeBinary(InputStream is, int length) throws IOException;

  /**
   * Write a binary field.
   */
  void writeBinaryField(String name, byte[] value) throws IOException;

  /**
   * Write a boolean field.
   */
  void writeBooleanField(String name, Boolean value) throws IOException;
}
