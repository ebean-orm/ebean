package com.avaje.ebean.text.json;

/**
 * Allows for customising the JSON write processing.
 * <p>
 * You can use this to add raw JSON content via {@link JsonWriter}.
 * <p>
 * You register a JsonWriteBeanVisitor with {@link JsonWriteOptions}.
 * </p>
 *
 * @param <T>
 *          the type of entity bean
 *
 * @see JsonWriteOptions
 */
public interface JsonWriteBeanVisitor<T> {

  /**
   * Visit the bean that has just been writing it's content to JSON. You can
   * write your own additional JSON content to the JsonWriter if you wish.
   *
   * @param bean
   *          the bean that has been writing it's content
   * @param jsonWriter
   *          the JsonWriter which you can append custom json content to if you
   *          wish.
   */
  void visit(T bean, JsonWriter jsonWriter);

}
