package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.DbMigration;
import io.ebean.annotation.Index;
import io.ebean.annotation.Indices;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static io.ebean.util.AnnotationUtil.get;

public class AnnotationFind {

  public static Set<JoinColumn> joinColumns(Field field) {
    final JoinColumn col = get(field, JoinColumn.class);
    if (col != null) {
      return Collections.singleton(col);
    }
    final JoinColumns cols = get(field, JoinColumns.class);
    if (cols != null) {
      Set<JoinColumn> result = new LinkedHashSet<>();
      Collections.addAll(result, cols.value());
      return result;
    }
    return Collections.emptySet();
  }

  public static Set<AttributeOverride> attributeOverrides(Field field) {
    final AttributeOverride ann = get(field, AttributeOverride.class);
    if (ann != null) {
      return Collections.singleton(ann);
    }
    final AttributeOverrides collection = get(field, AttributeOverrides.class);
    if (collection != null) {
      Set<AttributeOverride> result = new LinkedHashSet<>();
      Collections.addAll(result, collection.value());
      return result;
    }
    return Collections.emptySet();
  }

  public static Set<Index> indexes(Field field) {
    final Index ann = get(field, Index.class);
    if (ann != null) {
      return Collections.singleton(ann);
    }
    final Indices collection = get(field, Indices.class);
    if (collection != null) {
      Set<Index> result = new LinkedHashSet<>();
      Collections.addAll(result, collection.value());
      return result;
    }
    return Collections.emptySet();
  }

  public static Set<DbMigration> dbMigrations(Field field) {
    final DbMigration ann = get(field, DbMigration.class);
    if (ann != null) {
      return Collections.singleton(ann);
    }
    final DbMigration.List collection = get(field, DbMigration.List.class);
    if (collection != null) {
      Set<DbMigration> result = new LinkedHashSet<>();
      Collections.addAll(result, collection.value());
      return result;
    }
    return Collections.emptySet();
  }
}
