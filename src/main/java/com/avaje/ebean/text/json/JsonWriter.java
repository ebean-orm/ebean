package com.avaje.ebean.text.json;

import com.fasterxml.jackson.core.JsonGenerator;

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
   * Write a field name followed by object start.
   */
  void writeStartObject(String key);

  /**
   * Write a object start.
   */
  void writeStartObject();

  /**
   * Write a object end.
   */
  void writeEndObject();

  /**
   * Write a field name followed by array start.
   */
  void writeStartArray(String key);

  /**
   * Write a array start.
   */
  void writeStartArray();

  /**
   * Write a array end.
   */
  void writeEndArray();

  /**
   * Write the field name.
   */
  void writeFieldName(String name);

  /**
   * Write a null value taking into account null value suppression.
   */
  void writeNullField(String name);

  /**
   * Write a number field.
   */
  void writeNumberField(String name, int value);

  /**
   * Write a number field.
   */
  void writeNumberField(String name, short value);

  /**
   * Write a number field.
   */
  void writeNumberField(String name, long value);

  /**
   * Write a number field.
   */
  void writeNumberField(String name, double value);

  /**
   * Write a number field.
   */
  void writeNumberField(String name, float value);

  /**
   * Write a number field.
   */
  void writeNumberField(String name, BigDecimal value);

  /**
   * Write a sting field.
   */
  void writeStringField(String name, String value);

  /**
   * Write a binary field.
   */
  void writeBinary(InputStream is, int length);

  /**
   * Write a binary field.
   */
  void writeBinaryField(String name, byte[] value);

  /**
   * Write a boolean field.
   */
  void writeBooleanField(String name, boolean value);

  /**
   * Write a boolean value (typically inside a list).
   */
  void writeBoolean(boolean value);

  /**
   * Write a string value (typically inside a list).
   */
  void writeString(String value);

  /**
   * Write a int value (typically inside a list).
   */
  void writeNumber(int value);

  /**
   * Write a long value (typically inside a list).
   */
  void writeNumber(long value);

  /**
   * Write a BigDecimal value (typically inside a list).
   */
  void writeNumber(BigDecimal value);

  /**
   * Write a null value.
   */
  void writeNull();

  /**
   * Method that will force generator to copy
   * input text verbatim with <b>no</b> modifications (including
   * that no escaping is done and no separators are added even
   * if context [array, object] would otherwise require such).
   * If such separators are desired, use
   * {@link #writeRawValue(String)} instead.
   */
  void writeRaw(String text);

  /**
   * Method that will force generator to copy
   * input text verbatim without any modifications, but assuming
   * it must constitute a single legal JSON value (number, string,
   * boolean, null, Array or List). Assuming this, proper separators
   * are added if and as needed (comma or colon), and generator
   * state updated to reflect this.
   */
  void writeRawValue(String text);
}
