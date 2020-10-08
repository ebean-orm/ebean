package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.Aggregation;
import io.ebean.annotation.Avg;
import io.ebean.annotation.DbMigration;
import io.ebean.annotation.Index;
import io.ebean.annotation.Indices;
import io.ebean.annotation.Max;
import io.ebean.annotation.Min;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Sum;
import io.ebean.config.NamingConvention;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import javax.persistence.AttributeOverride;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;

/**
 * Provides some base methods for processing deployment annotations. All findAnnotation* methods
 * are capable to search for meta-annotations (annotation that has an other annotation)
 * <p>
 * <p>search algorithm for ONE annotation:</p>
 * <ul>
 * <li>Check if annotation is direct on the property</li>
 * <li>if not found: Check all annotations at the annotateElement
 * if they have the needed annotation as meta annotation</li>
 * <li>if not found: go up to super class and try again
 * (only findAnnotationRecursive)</li>
 * </ul>
 * DFS (Depth-First-Search) is used. The algorithm is the same as it is used in Spring-Framework,
 * as the code is taken from there.
 * <p>
 * <p>search algoritm for a Set&lt;Annotation&gt; works a litte bit different, as it does not stop
 * on the first match, but continues searching down to the last corner to find all annotations.</p>
 * <p>
 * <p>To prevent endless recursion, the search algoritm tracks all visited annotations</p>
 * <p>
 * <p>Supports also "java 1.6 repeatable containers" like{@link JoinColumn} / {@link JoinColumns}.</p>
 * <p>
 * <p>This means, searching for <code>JoinColumn</code> will find them also if they are inside a
 * <code>JoinColumn<b>s</b></code> annotation</p>
 */
abstract class AnnotationBase {

  final DatabasePlatform databasePlatform;
  protected final Platform platform;
  final NamingConvention namingConvention;
  final DeployUtil util;

  AnnotationBase(DeployUtil util) {
    this.util = util;
    this.databasePlatform = util.getDbPlatform();
    this.platform = databasePlatform.getPlatform();
    this.namingConvention = util.getNamingConvention();
  }

  /**
   * read the deployment annotations.
   */
  public abstract void parse();

  /**
   * Checks string is null or empty .
   */
  boolean isEmpty(String s) {
    return s == null || s.trim().isEmpty();
  }

  <T extends Annotation> T get(DeployBeanProperty prop, Class<T> annClass) {
    return AnnotationUtil.get(prop.getField(), annClass);
  }

  <T extends Annotation> boolean has(DeployBeanProperty prop, Class<T> annClass) {
    return AnnotationUtil.has(prop.getField(), annClass);
  }

  Set<JoinColumn> annotationJoinColumns(DeployBeanProperty prop) {
    return AnnotationFind.joinColumns(prop.getField());
  }

  Set<AttributeOverride> annotationAttributeOverrides(DeployBeanProperty prop) {
    return AnnotationFind.attributeOverrides(prop.getField());
  }

  Set<Index> annotationIndexes(DeployBeanProperty prop) {
    return AnnotationFind.indexes(prop.getField());
  }

  Set<DbMigration> annotationDbMigrations(DeployBeanProperty prop) {
    return AnnotationFind.dbMigrations(prop.getField());
  }

  Set<Index> annotationClassIndexes(Class<?> cls) {
    Set<Index> result = AnnotationUtil.typeGetAll(cls, Index.class);
    for (Indices index : AnnotationUtil.typeGetAll(cls, Indices.class)) {
      Collections.addAll(result, index.value());
    }
    return result;
  }

  Set<NamedQuery> annotationClassNamedQuery(Class<?> cls) {
    Set<NamedQuery> result = AnnotationUtil.typeGetAll(cls, NamedQuery.class);
    for (NamedQueries queries : AnnotationUtil.typeGetAll(cls, NamedQueries.class)) {
      Collections.addAll(result, queries.value());
    }
    return result;
  }
}
