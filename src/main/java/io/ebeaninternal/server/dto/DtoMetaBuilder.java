package io.ebeaninternal.server.dto;

import io.ebeaninternal.server.type.TypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Build the DtoMeta for a bean.
 * <p>
 * Use TypeManager to map bean property types to ScalarTypes.
 */
class DtoMetaBuilder {

  private static final Logger log = LoggerFactory.getLogger(DtoMetaBuilder.class);

  private final TypeManager typeManager;

  private final Class<?> dtoType;

  private final List<DtoMetaProperty> properties = new ArrayList<>();

  private final List<DtoMetaConstructor> constructorList = new ArrayList<>();

  DtoMetaBuilder(Class<?> dtoType, TypeManager typeManager) {
    this.dtoType = dtoType;
    this.typeManager = typeManager;
  }

  public DtoMeta build() throws IntrospectionException {

    readConstructors();
    readProperties();

    return new DtoMeta(dtoType, constructorList, properties);
  }

  private void readProperties() throws IntrospectionException {

    BeanInfo beanInfo = Introspector.getBeanInfo(dtoType);
    for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
      if (include(propertyDescriptor)) {
        try {
          properties.add(new DtoMetaProperty(typeManager, propertyDescriptor, dtoType));
        } catch (Exception e) {
          log.debug("exclude on " + dtoType + " property " + propertyDescriptor.getName(), e);
        }
      }
    }
  }

  private void readConstructors() {

    Constructor<?>[] constructors = dtoType.getConstructors();

    for (Constructor<?> constructor : constructors) {
      try {
        constructorList.add(new DtoMetaConstructor(typeManager, constructor, dtoType));
      } catch (Exception e) {
        // we don't want that constructor
        log.debug("exclude on " + dtoType + " constructor " + constructor, e);
      }
    }
  }

  private boolean include(PropertyDescriptor property) {
    return !property.getName().equals("class");
  }

}
