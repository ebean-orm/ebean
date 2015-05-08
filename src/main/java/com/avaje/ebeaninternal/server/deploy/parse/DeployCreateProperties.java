package com.avaje.ebeaninternal.server.deploy.parse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.persistence.ManyToOne;
import javax.persistence.PersistenceException;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.annotation.ColumnHstore;
import com.avaje.ebeaninternal.server.core.Message;
import com.avaje.ebeaninternal.server.deploy.DetermineManyType;
import com.avaje.ebeaninternal.server.deploy.ManyType;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertyCompound;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanPropertySimpleCollection;
import com.avaje.ebeaninternal.server.type.CtCompoundType;
import com.avaje.ebeaninternal.server.type.ScalarType;
import com.avaje.ebeaninternal.server.type.ScalarTypePostgresHstore;
import com.avaje.ebeaninternal.server.type.TypeManager;
import com.avaje.ebeaninternal.server.type.reflect.CheckImmutableResponse;

/**
 * Create the properties for a bean.
 * <p>
 * This also needs to determine if the property is a associated many, associated
 * one or normal scalar property.
 * </p>
 */
public class DeployCreateProperties {

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

    // check the transient properties...
    for (DeployBeanProperty prop : desc.propertiesAll()) {
      if (prop.isTransient()) {
        if (prop.getWriteMethod() == null || prop.getReadMethod() == null) {
          // Typically a helper method ... this is expected
          logger.trace("... transient: " + prop.getFullBeanName());
        } else {
          // dubious, possible error...
          String msg = Message.msg("deploy.property.nofield", desc.getFullName(), prop.getName());
          logger.warn(msg);
        }
      }
    }
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
    if (fieldName.startsWith("ajc$instance$")) {
      // ignore AspectJ internal fields
      return true;
    }

    // we are interested in this field
    return false;
  }

  /**
   * properties the bean properties from Class. Some of these properties may not map to database
   * columns.
   */
  private void createProperties(DeployBeanDescriptor<?> desc, Class<?> beanType, int level) {

    boolean scalaObject = desc.isScalaObject();

    try {
      Method[] declaredMethods = beanType.getDeclaredMethods();
      Field[] fields = beanType.getDeclaredFields();

      for (int i = 0; i < fields.length; i++) {

        Field field = fields[i];
        if (Modifier.isStatic(field.getModifiers())) {
          // not interested in static fields

        } else if (Modifier.isTransient(field.getModifiers())) {
          // not interested in transient fields
          logger.trace("Skipping transient field " + field.getName() + " in " + beanType.getName());

        } else if (ignoreFieldByName(field.getName())) {
          // not interested this field (ebean or aspectJ field)

        } else {

          String fieldName = getFieldName(field, beanType);
          String initFieldName = initCap(fieldName);

          Method getter = findGetter(field, initFieldName, declaredMethods, scalaObject);
          Method setter = findSetter(field, initFieldName, declaredMethods, scalaObject);

          DeployBeanProperty prop = createProp(desc, field, beanType, getter, setter);
          if (prop == null) {
            // transient annotation on unsupported type

          } else {
            // set a order that gives priority to inherited properties
            // push Id/EmbeddedId up and CreatedTimestamp/UpdatedTimestamp down
            int sortOverride = prop.getSortOverride();
            prop.setSortOrder((level * 10000 + 100 - i + sortOverride));

            DeployBeanProperty replaced = desc.addBeanProperty(prop);
            if (replaced != null) {
              if (replaced.isTransient()) {
                // expected for inheritance...
              } else {
                String msg = "Huh??? property " + prop.getFullBeanName() + " being defined twice";
                msg += " but replaced property was not transient? This is not expected?";
                logger.warn(msg);
              }
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

  /**
   * Make the first letter of the string upper case.
   */
  private String initCap(String str) {
    if (str.length() > 1) {
      return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    } else {
      // only a single char
      return str.toUpperCase();
    }
  }

  /**
   * Return the bean spec field name (trim of "is" from boolean types)
   */
  private String getFieldName(Field field, Class<?> beanType) {

    String name = field.getName();

    if ((Boolean.class.equals(field.getType()) || boolean.class.equals(field.getType())) && name.startsWith("is")
        && name.length() > 2) {

      // it is a boolean type field starting with "is"
      char c = name.charAt(2);
      if (Character.isUpperCase(c)) {
        String msg = "trimming off 'is' from boolean field name " + name + " in class " + beanType.getName();
        logger.info(msg);

        return name.substring(2);
      }
    }
    return name;
  }

  /**
   * Find a public non-static getter method that matches this field (according to bean-spec rules).
   */
  private Method findGetter(Field field, String initFieldName, Method[] declaredMethods, boolean scalaObject) {

    String methGetName = "get" + initFieldName;
    String methIsName = "is" + initFieldName;
    String scalaGet = field.getName();

    for (int i = 0; i < declaredMethods.length; i++) {
      Method m = declaredMethods[i];
      if ((scalaObject && m.getName().equals(scalaGet)) || m.getName().equals(methGetName)
          || m.getName().equals(methIsName)) {

        Class<?>[] params = m.getParameterTypes();
        if (params.length == 0) {
          if (field.getType().equals(m.getReturnType())) {
            int modifiers = m.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
              // we find it...
              return m;
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Find a public non-static setter method that matches this field (according to bean-spec rules).
   */
  private Method findSetter(Field field, String initFieldName, Method[] declaredMethods, boolean scalaObject) {

    String methSetName = "set" + initFieldName;
    String scalaSetName = field.getName() + "_$eq";

    for (int i = 0; i < declaredMethods.length; i++) {
      Method m = declaredMethods[i];

      if ((scalaObject && m.getName().equals(scalaSetName)) || m.getName().equals(methSetName)) {

        Class<?>[] params = m.getParameterTypes();
        if (params.length == 1 && field.getType().equals(params[0])) {
          if (void.class.equals(m.getReturnType())) {
            int modifiers = m.getModifiers();
            if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
              return m;
            }
          }
        }
      }
    }
    return null;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private DeployBeanProperty createManyType(DeployBeanDescriptor<?> desc, Class<?> targetType, ManyType manyType) {

    try {
      ScalarType<?> scalarType = typeManager.getScalarType(targetType);
      if (scalarType != null) {
        return new DeployBeanPropertySimpleCollection(desc, targetType, manyType);
      }
    } catch (NullPointerException e) {
      logger.debug("expected non-scalar type" + e.getMessage());
    }
    // TODO: Handle Collection of CompoundType and Embedded Type
    return new DeployBeanPropertyAssocMany(desc, targetType, manyType);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private DeployBeanProperty createProp(DeployBeanDescriptor<?> desc, Field field) {

    Class<?> propertyType = field.getType();
    
    ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
    
    if (manyToOne != null){
    	Class<?> tt = manyToOne.targetEntity();
    	
    	if (tt != null && !tt.equals(void.class)){
    		propertyType = tt;
    		logger.debug("target type" + tt);
    	}
    }
    Class<?> innerType = propertyType;

    String specialTypeKey = getSpecialScalarType(field);
    if (specialTypeKey != null) {
      ScalarType<?> scalarType = typeManager.getScalarTypeFromKey(specialTypeKey);
      if (scalarType == null) {
        logger.error("Could not find ScalarType to match key ["+specialTypeKey+"]");
      } else {
        return new DeployBeanProperty(desc, propertyType, scalarType, null);
      }
    }
    
    // check for Collection type (list, set or map)
    ManyType manyType = determineManyType.getManyType(propertyType);

    if (manyType != null) {
      // List, Set or Map based object
      Class<?> targetType = determineTargetType(field);
      if (targetType == null) {
        Transient transAnnotation = field.getAnnotation(Transient.class);
        if (transAnnotation != null) {
          // not supporting this field (generic type used)
          return null;
        }
        logger.warn("Could not find parameter type (via reflection) on " + desc.getFullName() + " " + field.getName());
      }
      return createManyType(desc, targetType, manyType);
    }

    if (innerType.isEnum() || innerType.isPrimitive()) {
      return new DeployBeanProperty(desc, propertyType, null, null);
    }

    ScalarType<?> scalarType = typeManager.getScalarType(innerType);
    if (scalarType != null) {
      return new DeployBeanProperty(desc, propertyType, scalarType, null);
    }

    CtCompoundType<?> compoundType = typeManager.getCompoundType(innerType);
    if (compoundType != null) {
      return new DeployBeanPropertyCompound(desc, propertyType, compoundType, null);
    }

    if (isTransientField(field)) {
      return null;
    }
    try {
      CheckImmutableResponse checkImmutable = typeManager.checkImmutable(innerType);
      if (checkImmutable.isImmutable()) {
        if (checkImmutable.isCompoundType()) {
          // use reflection to support compound immutable value objects
          typeManager.recursiveCreateScalarDataReader(innerType);
          compoundType = typeManager.getCompoundType(innerType);
          if (compoundType != null) {
            return new DeployBeanPropertyCompound(desc, propertyType, compoundType, null);
          }

        } else {
          // use reflection to support simple immutable value objects
          scalarType = typeManager.recursiveCreateScalarTypes(innerType);
          return new DeployBeanProperty(desc, propertyType, scalarType, null);
        }
      }

      return new DeployBeanPropertyAssocOne(desc, propertyType);

    } catch (Exception e) {
      logger.error("Error with " + desc + " field:" + field.getName(), e);
      return null;
    }
  }

  private String getSpecialScalarType(Field field) {

    if (field.getAnnotation(ColumnHstore.class) != null) {
      return ScalarTypePostgresHstore.KEY;
    }
    
    return null;
  }
  
  private boolean isTransientField(Field field) {

    Transient t = field.getAnnotation(Transient.class);
    return (t != null);
  }

  private DeployBeanProperty createProp(DeployBeanDescriptor<?> desc, Field field, Class<?> beanType,
      Method getter, Method setter) {

    DeployBeanProperty prop = createProp(desc, field);
    if (prop == null) {
      // transient annotation on unsupported type
      return null;
    } else {
      prop.setOwningType(beanType);
      prop.setName(field.getName());

      // the getter or setter could be null if we are using
      // javaagent type enhancement. If we are using subclass
      // generation then we do need to find the getter and setter
      prop.setReadMethod(getter);
      prop.setWriteMethod(setter);
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
        // probably a Set or List
        if (typeArgs[0] instanceof Class<?>) {
          return (Class<?>) typeArgs[0];
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
        return (Class<?>) typeArgs[1];
      }
    }
    // if targetType is null, then must be set in annotations
    return null;
  }
}
