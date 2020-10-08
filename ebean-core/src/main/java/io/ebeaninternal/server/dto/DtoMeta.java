package io.ebeaninternal.server.dto;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds property and constructor meta data for a given DTO bean type.
 *
 * Uses this to map a mapping request (columns) to a 'query plan' (constructor and setters).
 */
class DtoMeta {

  private final Class<?> dtoType;
  private final Map<String, DtoMetaProperty> propMap = new LinkedHashMap<>();

  private final Map<Integer, DtoMetaConstructor> constructorMap = new LinkedHashMap<>();

  private final DtoMetaConstructor defaultConstructor;
  private final DtoMetaConstructor maxArgConstructor;

  DtoMeta(Class<?> dtoType, List<DtoMetaConstructor> constructors, List<DtoMetaProperty> properties) {
    this.dtoType = dtoType;

    for (DtoMetaProperty property : properties) {
      propMap.put(property.getName().toUpperCase(), property);
    }

    int maxArg = 0;

    DtoMetaConstructor defaultConstructor = null;
    DtoMetaConstructor maxArgConstructor = null;

    for (DtoMetaConstructor constructor : constructors) {
      int args = constructor.getArgCount();
      constructorMap.put(args, constructor);
      if (args == 0) {
        defaultConstructor = constructor;
      } else if (args > maxArg) {
        maxArgConstructor = constructor;
        maxArg = args;
      }
    }
    this.defaultConstructor = defaultConstructor;
    this.maxArgConstructor = maxArgConstructor;
  }

  public DtoQueryPlan match(DtoMappingRequest request) {

    DtoColumn[] cols = request.getColumnMeta();

    int colLen = cols.length;
    DtoMetaConstructor constructor = constructorMap.get(colLen);
    if (constructor != null) {
      return new DtoQueryPlanConstructor(request, constructor);
    }
    if (maxArgConstructor != null && colLen > maxArgConstructor.getArgCount()) {
      // maxArgConst + setters
      return matchMaxArgPlusSetters(request);
    }
    if (defaultConstructor != null) {
      return matchSetters(request);
    }

    String msg = "Unable to map the resultSet columns " + Arrays.toString(cols)
      + " to the bean type ["+dtoType+"] as the number of columns in the resultSet is less than the constructor"
      + " (and that there is no default constructor) ?";
    throw new IllegalStateException(msg);
  }

  private DtoQueryPlanConPlus matchMaxArgPlusSetters(DtoMappingRequest request) {
    DtoReadSet[] setterProps = request.mapArgPlusSetters(this, maxArgConstructor.getArgCount());
    return new DtoQueryPlanConPlus(request, maxArgConstructor, setterProps);
  }

  private DtoQueryPlan matchSetters(DtoMappingRequest request) {
    DtoReadSet[] setterProps = request.mapSetters(this);
    return new DtoQueryPlanConSetter(request, defaultConstructor, setterProps);
  }

  DtoReadSet findProperty(String label) {
    String upperLabel = label.toUpperCase();
    DtoMetaProperty property = propMap.get(upperLabel);
    if (property == null && upperLabel.startsWith("IS_")) {
      property = propMap.get(upperLabel.substring(3));
    }
    if (property == null) {
      property = propMap.get(upperLabel.replace("_", ""));
    }
    return property;
  }

  Class<?> dtoType() {
    return dtoType;
  }
}
