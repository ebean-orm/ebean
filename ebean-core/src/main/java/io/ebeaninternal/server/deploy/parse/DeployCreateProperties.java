package io.ebeaninternal.server.deploy.parse;

import io.ebean.Model;
import io.ebean.annotation.DbArray;
import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonB;
import io.ebean.annotation.DbMap;
import io.ebean.annotation.UnmappedJson;
import io.ebean.core.type.ScalarType;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.server.deploy.DetermineManyType;
import io.ebeaninternal.server.deploy.ManyType;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertySimpleCollection;
import io.ebeaninternal.server.type.TypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.ManyToOne;
import javax.persistence.PersistenceException;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Create the properties for a bean.
 * <p>
 * This also needs to determine if the property is a associated many, associated
 * one or normal scalar property.
 * </p>
 */
public final class DeployCreateProperties {

  private static final Logger logger = LoggerFactory.getLogger(DeployCreateProperties.class);

  private final DetermineManyType determineManyType;
  private final TypeManager typeManager;

  public DeployCreateProperties(TypeManager typeManager) {
    this.typeManager = typeManager;
    this.determineManyType = new DetermineManyType();
  }

  /**
   * Create the appropriate properties for a bean.
   */
  public void createProperties(DeployBeanDescriptor<?> desc) {
    createProperties(desc, desc.getBeanType(), 0);
    desc.sortProperties();
  }

  /**
   * Return true if we should ignore this field.
   * <p>
   * We want to ignore ebean internal fields and some others as well.
   * </p>
   */
  private boolean ignoreFieldByName(String fieldName) {
    if (fieldName.startsWith("_ebean_")) {
      // ignore Ebean internal fields
      return true;
    }
    // ignore AspectJ internal fields
    return fieldName.startsWith("ajc$instance$");
  }

  private boolean ignoreField(Field field) {
    return Modifier.isStatic(field.getModifiers())
      || Modifier.isTransient(field.getModifiers())
      || ignoreFieldByName(field.getName());
  }

  /**
   * properties the bean properties from Class. Some of these properties may not map to database
   * columns.
   */
  private void createProperties(DeployBeanDescriptor<?> desc, Class<?> beanType, int level) {
    if (beanType.equals(Model.class)) {
      // ignore all fields on model (_$dbName)
      return;
    }
    try {
      Field[] fields = beanType.getDeclaredFields();
      for (int i = 0; i < fields.length; i++) {
        Field field = fields[i];
        if (!ignoreField(field)) {
          DeployBeanProperty prop = createProp(desc, field, beanType);
          if (prop != null) {
            // set a order that gives priority to inherited properties
            // push Id/EmbeddedId up and CreatedTimestamp/UpdatedTimestamp down
            int sortOverride = prop.getSortOverride();
            prop.setSortOrder((level * 10000 + 100 - i + sortOverride));

            DeployBeanProperty replaced = desc.addBeanProperty(prop);
            if (replaced != null && !replaced.isTransient()) {
              String msg = "Huh??? property " + prop.getFullBeanName() + " being defined twice";
              msg += " but replaced property was not transient? This is not expected?";
              logger.warn(msg);
            }
          }
        }
      }

      Class<?> superClass = beanType.getSuperclass();
      if (!superClass.equals(Object.class)) {
        // recursively add any properties in the inheritance hierarchy
        // up to the Object.class level...
        createProperties(desc, superClass, level + 1);
      }
    } catch (PersistenceException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new PersistenceException(ex);
    }
  }

  @SuppressWarnings({"unchecked"})
  private DeployBeanProperty createManyType(DeployBeanDescriptor<?> desc, Class<?> targetType, ManyType manyType) {
    try {
      ScalarType<?> scalarType = typeManager.getScalarType(targetType);
      if (scalarType != null) {
        return new DeployBeanPropertySimpleCollection(desc, targetType, manyType);
      }
    } catch (NullPointerException e) {
      logger.debug("expected non-scalar type {}", e.getMessage());
    }
    return new DeployBeanPropertyAssocMany(desc, targetType, manyType);
  }

  @SuppressWarnings({"unchecked"})
  private DeployBeanProperty createProp(DeployBeanDescriptor<?> desc, Field field) {
    Class<?> propertyType = field.getType();
    if (isSpecialScalarType(field)) {
      return new DeployBeanProperty(desc, propertyType, field.getGenericType());
    }
    // check for Collection type (list, set or map)
    ManyType manyType = determineManyType.getManyType(propertyType);
    if (manyType != null) {
      // List, Set or Map based object
      Class<?> targetType = determineTargetType(field);
      if (targetType == null) {
        if (AnnotationUtil.has(field, Transient.class)) {
          // not supporting this field (generic type used)
          return null;
        }
        logger.warn("Could not find parameter type (via reflection) on " + desc.getFullName() + " " + field.getName());
      }
      return createManyType(desc, targetType, manyType);
    }
    if (propertyType.isEnum() || propertyType.isPrimitive()) {
      return new DeployBeanProperty(desc, propertyType, null, null);
    }
    ScalarType<?> scalarType = typeManager.getScalarType(propertyType);
    if (scalarType != null) {
      return new DeployBeanProperty(desc, propertyType, scalarType, null);
    }
    if (isTransientField(field)) {
      // return with no ScalarType (still support JSON features)
      return new DeployBeanProperty(desc, propertyType, null, null);
    }
    try {
      return new DeployBeanPropertyAssocOne(desc, propertyType);

    } catch (Exception e) {
      logger.error("Error with " + desc + " field:" + field.getName(), e);
      return null;
    }
  }

  /**
   * Return true if the field has one of the special mappings.
   */
  private boolean isSpecialScalarType(Field field) {
    return (AnnotationUtil.has(field, DbJson.class))
      || (AnnotationUtil.has(field, DbJsonB.class))
      || (AnnotationUtil.has(field, DbArray.class))
      || (AnnotationUtil.has(field, DbMap.class))
      || (AnnotationUtil.has(field, UnmappedJson.class));
  }

  private boolean isTransientField(Field field) {
    return AnnotationUtil.has(field, Transient.class);
  }

  private DeployBeanProperty createProp(DeployBeanDescriptor<?> desc, Field field, Class<?> beanType) {
    DeployBeanProperty prop = createProp(desc, field);
    if (prop == null) {
      // transient annotation on unsupported type
      return null;
    } else {
      prop.setOwningType(beanType);
      prop.setName(field.getName());
      prop.setField(field);
      return prop;
    }
  }

  /**
   * Determine the type of the List,Set or Map. Not been set explicitly so determine this from
   * ParameterizedType.
   */
  private Class<?> determineTargetType(Field field) {
    Type genType = field.getGenericType();
    if (genType instanceof ParameterizedType) {
      ParameterizedType ptype = (ParameterizedType) genType;
      Type[] typeArgs = ptype.getActualTypeArguments();
      if (typeArgs.length == 1) {
        // expecting set or list
        if (typeArgs[0] instanceof Class<?>) {
          return (Class<?>) typeArgs[0];
        }
        if (typeArgs[0] instanceof WildcardType) {
          final Type[] upperBounds = ((WildcardType) typeArgs[0]).getUpperBounds();
          if (upperBounds.length == 1 && upperBounds[0] instanceof Class<?>) {
            // kotlin generated wildcard type
            return (Class<?>) upperBounds[0];
          }
        }
        // throw new RuntimeException("Unexpected Parameterised Type? "+typeArgs[0]);
        return null;
      }
      if (typeArgs.length == 2) {
        // this is probably a Map
        if (typeArgs[1] instanceof ParameterizedType) {
          // not supporting ParameterizedType on Map.
          return null;
        }
        if (typeArgs[1] instanceof WildcardType) {
          return Object.class;
        }
        return (Class<?>) typeArgs[1];
      }
    }
    // if targetType is null, then must be set in annotations
    return null;
  }
}
