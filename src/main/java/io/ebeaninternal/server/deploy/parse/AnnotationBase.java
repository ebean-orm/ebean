package io.ebeaninternal.server.deploy.parse;

import io.ebean.annotation.Aggregation;
import io.ebean.annotation.Avg;
import io.ebean.annotation.Max;
import io.ebean.annotation.Min;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Sum;
import io.ebean.annotation.Where;
import io.ebean.config.NamingConvention;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
  private final Platform platform;
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

  /**
   * True if the annotation platforms match the current platform.
   */
  private boolean matchPlatform(Platform[] platforms) {
    if (platforms.length == 0) {
      return true;
    } else {
      for (Platform annPlatform : platforms) {
        if (annPlatform == platform) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Return the annotation for the property - including meta-annotations and platform filter.
   */
  <T extends Annotation> T getMeta(DeployBeanProperty prop, Class<T> annClass) {
    return AnnotationUtil.findPlatform(prop.getField(), annClass, platform);
  }

  <T extends Annotation> T get(DeployBeanProperty prop, Class<T> annClass) {
    return AnnotationUtil.get(prop.getField(), annClass);
  }

  <T extends Annotation> boolean has(DeployBeanProperty prop, Class<T> annClass) {
    return AnnotationUtil.has(prop.getField(), annClass);
  }

  /**
   * Return all annotations for this property. Annotations are not filtered by platform and you'll get
   * really all annotations that are directly, indirectly or meta-present.
   */
  <T extends Annotation> Set<T> getAll(DeployBeanProperty prop, Class<T> annClass) {
    return AnnotationUtil.findAll(prop.getField(), annClass);
  }

  /**
   * Return the annotation for the property.
   * <p>
   * Looks first at the field and then at the getter method. then at class level.
   * (This is used for SequenceGenerator e.g.)
   * </p>
   */
  <T extends Annotation> T find(DeployBeanProperty prop, Class<T> annClass) {
    T a = get(prop, annClass);
    if (a == null) {
      a = AnnotationUtil.findPlatform(prop.getOwningType(), annClass, platform);
    }
    return a;
  }

  String aggregation(DeployBeanProperty prop) {
    final Field field = prop.getField();
    Aggregation agg = AnnotationUtil.get(field, Aggregation.class);
    if (agg != null) {
      return agg.value();
    }
    Max max = AnnotationUtil.get(field, Max.class);
    if (max != null) {
      return "max($1)";
    }
    Min min = AnnotationUtil.get(field, Min.class);
    if (min != null) {
      return "min($1)";
    }
    Sum sum = AnnotationUtil.get(field, Sum.class);
    if (sum != null) {
      return "sum($1)";
    }
    Avg avg = AnnotationUtil.get(field, Avg.class);
    if (avg != null) {
      return "avg($1)";
    }
    return null;
  }

  /**
   * Return the Where search including repeatable and platform filtering.
   */
  Where platformAnnotationWhere(DeployBeanProperty prop) {
    final Where where = AnnotationUtil.get(prop.getField(), Where.class);
    if (where != null && matchPlatform(where.platforms())) {
      return where;
    }
    final Where.List list = AnnotationUtil.get(prop.getField(), Where.List.class);
    if (list != null) {
      for (Where listWhere : list.value()) {
        if (matchPlatform(listWhere.platforms())){
          return listWhere;
        }

      }
    }
    return null;
  }
}
