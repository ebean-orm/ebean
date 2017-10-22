package io.ebean.text.json;

import java.io.IOException;

import io.ebean.plugin.BeanType;
import io.ebeaninternal.server.text.json.ReadJson;

/**
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
@FunctionalInterface
public interface JsonVersionMigrationHandler {

  ReadJson migrate(ReadJson readJson, BeanType<?> desc) throws IOException;

}
