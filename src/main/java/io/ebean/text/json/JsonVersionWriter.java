package io.ebean.text.json;

import io.ebean.plugin.BeanType;

/**
 * Can be applied to JsonWriteOptions to write a json data version in front of the bean.
 *
 * Use it in conjunction with {@link JsonVersionMigrationHandler} to convert older json versions back to your beans.
 *
 * <p>For example you can read a static field from your model:</p><pre>
 * &#64;DocStore
 * public class MyBean {
 *   private static final int BEAN_VERSION = 3;
 *   ...
 * }
 * ...
 * opts.setVersionWriter((writer, desc) -> {
 *   try {
 *     writer.writeNumberField("_bv", desc.getBeanType().getDeclaredField("BEAN_VERSION").get(null));
 *   } catch (Exception e) {}
 * });</pre>
 * @author Roland Praml, FOCONIS AG
 *
 */
@FunctionalInterface
public interface JsonVersionWriter {

  /**
   * Write the version of "beanType". An implementation could be to read a static field from the concrete bean class.
   */
  void writeVersion(JsonWriter writer, BeanType<?> beanType);

}
