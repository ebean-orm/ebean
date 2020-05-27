package io.ebeaninternal.server.dto;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ebean.util.StringHelper.replaceString;

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


    int firstOnes = maxArgConstructor.getArgCount();

    DtoColumn[] cols = request.getColumnMeta();

    DtoReadSet[] setterProps = new DtoReadSet[cols.length - firstOnes];

    int pos = 0;
    for (int i = firstOnes; i < cols.length; i++) {
      String label = cols[i].getLabel();
      DtoReadSet property = findProperty(label);
      if (property == null || property.isReadOnly()) {
        if (request.isRelaxedMode()) {
          property = DtoReadSetColumnSkip.INSTANCE;
        } else {
          throw new IllegalStateException(unableToMapColumnMessage(cols[i]));
        }
      }
      setterProps[pos++] = property;
    }

    return new DtoQueryPlanConPlus(request, maxArgConstructor, setterProps);
  }

  private String unableToMapColumnMessage(DtoColumn col) {
    return "Unable to map DB column " + col + " to a property with a setter method on " + dtoType+". Consider query.setRelaxedMode() to skip mapping this column.";
  }

  private DtoQueryPlan matchSetters(DtoMappingRequest request) {

    DtoColumn[] cols = request.getColumnMeta();

    DtoReadSet[] setterProps = new DtoReadSet[cols.length];

    for (int i = 0; i < cols.length; i++) {
      String label = cols[i].getLabel();
      DtoReadSet property = findProperty(label);
      if (property == null || property.isReadOnly()) {
        if (request.isRelaxedMode()) {
          property = DtoReadSetColumnSkip.INSTANCE;
        } else {
          throw new IllegalStateException(unableToMapColumnMessage(cols[i]));
        }
      }
      setterProps[i] = property;
    }

    return new DtoQueryPlanConSetter(request, defaultConstructor, setterProps);
  }

  private DtoReadSet findProperty(String label) {

    String upperLabel = label.toUpperCase();
    DtoMetaProperty property = propMap.get(upperLabel);
    if (property == null && upperLabel.startsWith("IS_")) {
      property = propMap.get(upperLabel.substring(3));
    }
    if (property == null) {
      property = propMap.get(replaceString(upperLabel, "_", ""));
    }
    return property;
  }
}
