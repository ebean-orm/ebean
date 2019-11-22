package io.ebean.text.json;


import java.io.IOException;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.ebean.plugin.BeanType;

/**
 * This handler can perform JSON migration. (Similar to migration scripts)
 *
 * <b>Note:</b> If you have set a MigrationHandler, you will loose a bit performance, because every model will be converted first
 * to an ObjectNode and then back to a parser. I.e. the json-data has to be read completely into memory, perform the migration and
 * then distribute the json values to the bean-properties.
 *
 * Without a MigrationHandler, Ebean will use the jackson streaming API for non-inherited beans and distribute the json values
 * directly as they come from the json stream. The disadvantages are, that you cannot access the property values in random.
 *
 * A concrete implementation could compare the dataVersion stored in the node with the dataVersion
 * @author Roland Praml, FOCONIS AG
 */
public interface JsonVersionMigrationHandler {

  /**
   * Migrates a root bean of an inheritance tree. <code>beanType</code> is the root of the inheritance tree.
   * This method is only for beans, that have an inheritance. You may return the parameter node or create a new ObjectNode.
   */
  @Nonnull
  ObjectNode migrateRoot(@Nonnull ObjectNode node, @Nonnull ObjectMapper mapper, @Nonnull BeanType<?> rootBeanType) throws IOException;

  /**
   * Migrates a concrete bean of <code>beanType</code>. You may return the parameter node or create a new ObjectNode.
   */
  @Nonnull
  ObjectNode migrate(@Nonnull ObjectNode node, @Nonnull ObjectMapper mapper, @Nonnull BeanType<?> beanType) throws IOException;

}
