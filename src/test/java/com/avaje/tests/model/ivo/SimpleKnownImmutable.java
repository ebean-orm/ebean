package com.avaje.tests.model.ivo;

import com.avaje.ebeaninternal.server.type.reflect.KnownImmutable;

public class SimpleKnownImmutable implements KnownImmutable {

  public boolean isKnownImmutable(Class<?> cls) {

    // Check for all allowed property types...
    if (cls.isPrimitive() || String.class.equals(cls) || Object.class.equals(cls)) {
      return true;
    }
    if (java.util.Date.class.equals(cls) || java.sql.Date.class.equals(cls) || java.sql.Timestamp.class.equals(cls)) {
      // treat as immutable even through they are not strictly so
      return true;
    }
    if (java.math.BigDecimal.class.equals(cls) || java.math.BigInteger.class.equals(cls)) {
      // treat as immutable (contain non-final fields)
      return true;
    }

    if (Integer.class.equals(cls) || Long.class.equals(cls) || Double.class.equals(cls) || Float.class.equals(cls)
        || Short.class.equals(cls) || Byte.class.equals(cls) || Character.class.equals(cls)
        || Boolean.class.equals(cls)) {
      return true;
    }

    return false;
  }
}
