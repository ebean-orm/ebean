package io.ebeaninternal.server.persist.dml;

import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shared support for the platform specific insert on conflict do update generation.
 */
final class InsertMetaOptionsSupport {

  private InsertMetaOptionsSupport() {
  }

  /**
   * Return the columns to use as the on conflict target.
   * <p>
   * Uses columns explicitly mapped as unique via {@code @Column(unique=true)} or
   * {@code @Index(unique=true)}. When there are none of those, falls back to the primary
   * key column(s) given the primary key constraint is inherently unique. This fallback only
   * applies when the id value is included in the insert (withId) as an insert relying on a
   * database generated id (identity/sequence) is never going to naturally conflict on that id.
   */
  static List<String> uniqueColumns(BeanDescriptor<?> desc, boolean withId) {
    List<String> uniqueColumns = desc.uniqueProps().stream()
      .flatMap(Arrays::stream)
      .map(BeanProperty::dbColumn)
      .collect(Collectors.toList());
    if (uniqueColumns.isEmpty() && withId) {
      uniqueColumns = idColumns(desc);
    }
    return uniqueColumns;
  }

  private static List<String> idColumns(BeanDescriptor<?> desc) {
    BeanProperty idProperty = desc.idProperty();
    if (idProperty == null) {
      return List.of();
    }
    if (idProperty.isEmbedded() && idProperty instanceof BeanPropertyAssocOne) {
      List<String> columns = new ArrayList<>();
      for (BeanProperty embedded : ((BeanPropertyAssocOne<?>) idProperty).properties()) {
        columns.add(embedded.dbColumn());
      }
      return columns;
    }
    return List.of(idProperty.dbColumn());
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
