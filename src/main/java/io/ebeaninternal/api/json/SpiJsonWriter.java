package io.ebeaninternal.api.json;

import io.ebean.bean.EntityBean;
import io.ebean.text.json.JsonWriter;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.io.IOException;
import java.util.Collection;

/**
 * Internal API extensions for JSON writing of Bean properties.
 */
public interface SpiJsonWriter extends JsonWriter {

  /**
   * Flush the buffer.
   */
  void flush() throws IOException;

  /**
   * Return true if the value is a parent bean.
   */
  boolean isParentBean(Object value);

  /**
   * Start an assoc one path.
   */
  void beginAssocOne(String name, EntityBean bean);

  /**
   * End an assoc one path.
   */
  void endAssocOne();

  /**
   * Return true if the many property should be included.
   */
  Boolean includeMany(String name);

  /**
   * Push the parent bean of a ToMany.
   */
  void pushParentBeanMany(EntityBean bean);

  /**
   * Pop the parent of a ToMany.
   */
  void popParentBeanMany();

  /**
   * Write the collection.
   */
  void toJson(String name, Collection<?> collection);

  /**
   * Start a Many.
   */
  void beginAssocMany(String name);

  /**
   * End a Many.
   */
  void endAssocMany();

  /**
   * Write value using underlying Jaskson object mapper if available.
   */
  void writeValueUsingObjectMapper(String name, Object value);

  /**
   * Write the bean properties.
   */
  <T> void writeBean(BeanDescriptor<T> desc, EntityBean bean);
}
