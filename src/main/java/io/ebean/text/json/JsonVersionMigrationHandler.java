package io.ebean.text.json;

import java.io.IOException;

import io.ebean.plugin.BeanType;
import io.ebeaninternal.server.text.json.ReadJson; // FIXME: should not import ebeaninternal here

/**
 * This handler can perform JSON migration. (Similar to migration scripts)
 *
 * @author Roland Praml, FOCONIS AG
 */
@FunctionalInterface
public interface JsonVersionMigrationHandler {

  ReadJson migrate(ReadJson readJson, BeanType<?> beanType) throws IOException;

}
