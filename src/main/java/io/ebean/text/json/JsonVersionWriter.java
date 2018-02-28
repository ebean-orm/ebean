package io.ebean.text.json;

import io.ebean.plugin.BeanType;
/**
 * Can be applied to JsonWriteOptions to write a json data version in front of the bean.
 *
 * Use it in conjunction with {@link JsonVersionMigrationHandler} to convert older json versions back to your beans.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
@FunctionalInterface
public interface JsonVersionWriter {

  void writeVersion(JsonWriter writer, BeanType<?> beanType);

}
