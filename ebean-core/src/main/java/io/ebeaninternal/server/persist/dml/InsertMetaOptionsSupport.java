package io.ebeaninternal.server.persist.dml;

import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared support for the platform specific insert on conflict do update generation.
 */
final class InsertMetaOptionsSupport {

  private InsertMetaOptionsSupport() {
  }

  /**
   * Return the columns that should be excluded from the generated "do update set" clause
   * as they are not updatable, e.g. {@code @Column(updatable=false)} or a generated property
   * that is insert only such as {@code @WhenCreated}/{@code @WhoCreated}.
   */
  static List<String> nonUpdatableColumns(BeanDescriptor<?> desc) {
    List<String> columns = new ArrayList<>();
    for (BeanProperty prop : desc.propertiesNonTransient()) {
      if (!prop.isDbUpdatable() || isInsertOnlyGenerated(prop)) {
        columns.add(prop.dbColumn());
      }
    }
    return columns;
  }

  private static boolean isInsertOnlyGenerated(BeanProperty prop) {
    GeneratedProperty gen = prop.generatedProperty();
    return gen != null && gen.includeInInsert() && !gen.includeInUpdate();
  }
}
