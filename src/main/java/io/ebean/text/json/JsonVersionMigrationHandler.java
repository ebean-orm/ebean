package io.ebean.text.json;

import java.io.IOException;

import io.ebean.plugin.BeanType;

/**
 * This handler can perform JSON migration. (Similar to migration scripts)
 *
 * @author Roland Praml, FOCONIS AG
 */
@FunctionalInterface
public interface JsonVersionMigrationHandler {

  JsonReader migrate(JsonReader readJson, BeanType<?> beanType) throws IOException;

}
