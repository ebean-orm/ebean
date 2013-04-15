package com.avaje.ebean.text.json;

/**
 * The JSON Writer made available to JsonWriteBeanVisitor's so that you can
 * append your own JSON content into the output.
 * 
 * @see JsonWriteBeanVisitor
 * @see JsonWriteOptions#setRootPathVisitor(JsonWriteBeanVisitor)
 * @see JsonWriteOptions#setPathVisitor(String, JsonWriteBeanVisitor)
 * 
 * @author rbygrave
 */
public interface JsonWriter {

  /**
   * Use this to append some custom content into the JSON output.
   * 
   * @param key
   *          the json key
   * 
   * @param rawJsonValue
   *          raw json value
   */
  public void appendRawValue(String key, String rawJsonValue);

  public void appendQuoteEscapeValue(String key, String rawJsonValue);

}
