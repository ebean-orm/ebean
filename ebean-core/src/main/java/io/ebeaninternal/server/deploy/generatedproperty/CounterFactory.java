package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;

/**
 * Creates "Counter" GeneratedProperty for various types of number.
 * <p>
 * Aka, Integer, Long, Short etc.
 * </p>
 */
final class CounterFactory {

  private final GeneratedCounterInteger integerCounter = new GeneratedCounterInteger();
  private final GeneratedCounterLong longCounter = new GeneratedCounterLong();

   void setCounter(DeployBeanProperty property) {
    property.setGeneratedProperty(createCounter(property));
  }

  /**
   * Create the GeneratedProperty based on the property type.
   */
  private GeneratedProperty createCounter(DeployBeanProperty property) {
    Class<?> propType = property.getPropertyType();
    if (propType.equals(Integer.class) || propType.equals(int.class)) {
      return integerCounter;
    }
    if (propType.equals(Long.class) || propType.equals(long.class)) {
      return longCounter;
    }
    int type = getType(propType);
    return new GeneratedCounter(type);
  }

  private int getType(Class<?> propType) {
    if (propType.equals(Short.class) || propType.equals(short.class)) {
      return Types.TINYINT;
    }
    if (propType.equals(BigDecimal.class)) {
      return Types.DECIMAL;
    }
    if (propType.equals(Double.class) || propType.equals(double.class)) {
      return Types.DOUBLE;
    }
    if (propType.equals(Float.class) || propType.equals(float.class)) {
      return Types.REAL;
    }
    if (propType.equals(BigInteger.class)) {
      return Types.BIGINT;
    }
    String msg = "Can not support Counter for type " + propType.getName();
    throw new PersistenceException(msg);
  }
}
