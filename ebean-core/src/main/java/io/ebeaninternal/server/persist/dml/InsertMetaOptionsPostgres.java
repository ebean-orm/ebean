package io.ebeaninternal.server.persist.dml;

import io.ebean.InsertOptions;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Postgres specific generation of insert on conflict.
 */
final class InsertMetaOptionsPostgres implements InsertMetaOptions {

  private final InsertMeta meta;
  private final BeanDescriptor<?> desc;
  private final String baseTable;
  private final Map<String, String> sqlCache = new ConcurrentHashMap<>();

  InsertMetaOptionsPostgres(InsertMeta meta, BeanDescriptor<?> desc) {
    this.meta = meta;
    this.desc = desc;
    this.baseTable = desc.baseTable();
  }

  @Override
  public String sql(boolean withId, InsertOptions options) {
    String key = withId + options.key();
    return sqlCache.computeIfAbsent(key, k -> generate(withId, options));
  }

  private String generate(boolean withId, InsertOptions options) {
    char type = options.key().charAt(0);
    switch (type) {
      case 'U':
        return generate(withId, false, options);
      case 'N':
        return generate(withId, true, options);
      default:
        return meta.sqlFor(withId);
    }
  }

  private String generate(boolean withId, boolean doNothing, InsertOptions options) {
    GenerateDmlRequest request = new GenerateDmlRequest();
    meta.sql(request, !withId, baseTable);
    request.append(" on conflict ");

    List<String> uniqueColumns = desc.uniqueProps().stream()
      .flatMap(Arrays::stream)
      .map(BeanProperty::dbColumn)
      .collect(Collectors.toList());

    String constraintName = options.constraint();
    if (constraintName != null) {
      request.append("on constraint ").append(constraintName);
    } else {
      request.append("(");
      String cols = options.uniqueColumns();
      if (cols != null) {
        request.append(cols);
      } else {
        appendUniqueColumns(uniqueColumns, request);
      }
      request.append(")");
    }
    if (doNothing) {
      request.append(" do nothing");
      return request.toString();
    }
    request.append(" do update set ");
    String updateSet = options.updateSet();
    if (updateSet != null) {
      request.append(updateSet);
    } else {
      setColumns(withId, request, uniqueColumns);
    }
    return request.toString();
  }

  private void setColumns(boolean withId, GenerateDmlRequest request, List<String> uniqueColumns) {
    List<String> columns = request.columns();
    columns.removeAll(uniqueColumns);
    if (withId) {
      BeanProperty idProperty = desc.idProperty();
      if (idProperty != null && !idProperty.isEmbedded()) {
        columns.remove(idProperty.dbColumn());
      }
    }
    for (int i = 0; i < columns.size(); i++) {
      if (i > 0) {
        request.append(", ");
      }
      String col = columns.get(i);
      request.append(col).append("=excluded.").append(col);
    }
  }

  private static void appendUniqueColumns(List<String> uniqueColumns, GenerateDmlRequest request) {
    if (uniqueColumns.isEmpty()) {
      throw new IllegalStateException("Unable to identify unique columns for INSERT ON CONFLICT - Add mapping like @Column(unique=true) or @Index(unique=true)");
    }
    for (int i = 0; i < uniqueColumns.size(); i++) {
      if (i > 0) {
        request.append(", ");
      }
      request.append(uniqueColumns.get(i));
    }
  }
}
