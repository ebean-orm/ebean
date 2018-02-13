package io.ebean.text.json;

import io.ebean.plugin.BeanType;
/**
 * Can be applied to JsonWriteOptions to write a json data version in front of the bean.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
@FunctionalInterface
public interface JsonWriteVersion {

  void write(JsonWriter write, BeanType<?> beanType);

}
