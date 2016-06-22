package com.avaje.tests.model.ivo;

import com.avaje.ebeaninternal.server.type.reflect.KnownImmutable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;

public class SimpleKnownImmutable implements KnownImmutable {

  public boolean isKnownImmutable(Class<?> cls) {

    // Check for all allowed property types...
    if (cls.isPrimitive() || String.class.equals(cls) || Object.class.equals(cls)) {
      return true;
    }
    if (Date.class.equals(cls) || Date.class.equals(cls) || Timestamp.class.equals(cls)) {
      // treat as immutable even through they are not strictly so
      return true;
    }
    if (BigDecimal.class.equals(cls) || BigInteger.class.equals(cls)) {
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
