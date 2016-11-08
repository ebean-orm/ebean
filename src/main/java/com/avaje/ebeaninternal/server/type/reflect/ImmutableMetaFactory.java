package com.avaje.ebeaninternal.server.type.reflect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class ImmutableMetaFactory {

  private static final Logger logger = LoggerFactory.getLogger(ImmutableMetaFactory.class);

  public ImmutableMeta createImmutableMeta(Class<?> cls) {

    ScoreConstructor[] scoreConstructors = scoreConstructors(cls);

    ArrayList<RuntimeException> errors = new ArrayList<>();

    // search the constructors in score order ...
    // ... we need to find a set of readers for each
    // ... type in the constructor
    for (ScoreConstructor scoreConstructor : scoreConstructors) {
      Constructor<?> constructor = scoreConstructor.constructor;

      try {
        Method[] getters = findGetters(cls, constructor);

        return new ImmutableMeta(constructor, getters);

      } catch (NoSuchMethodException e) {
        String msg = "Error finding getter method on " + cls + " with constructor " + constructor;
        errors.add(new RuntimeException(msg, e));
      }
    }

    String msg = "Was unable to use reflection to find a constructor and appropriate getters for" +
        "immutable type " + cls + ".  The errors while looking for the getter methods follow:";
    logger.error(msg);

    for (RuntimeException runtimeException : errors) {
      logger.error("Error with " + cls, runtimeException);
    }

    msg = "Unable to use reflection to build ImmutableMeta for " + cls
        + ".  Associated Errors trying to find a constructor and getter methods have been logged";

    throw new RuntimeException(msg);
  }

  private ScoreConstructor getScore(Constructor<?> c) {

    Class<?>[] parameterTypes = c.getParameterTypes();
    int score = -1000 * parameterTypes.length;

    for (Class<?> parameterType : parameterTypes) {
      if (parameterType.equals(String.class)) {
        // string is very generic and we would prefer
        // a more specific type if that was available
        score = score + 1;

      } else if (parameterType.equals(BigDecimal.class)) {
        score = score - 10;
      } else if (parameterType.equals(Timestamp.class)) {
        score = score - 10;
      } else if (parameterType.equals(double.class)) {
        score = score - 9;
      } else if (parameterType.equals(Double.class)) {
        score = score - 8;
      } else if (parameterType.equals(float.class)) {
        score = score - 7;
      } else if (parameterType.equals(Float.class)) {
        score = score - 6;
      } else if (parameterType.equals(long.class)) {
        score = score - 5;
      } else if (parameterType.equals(Long.class)) {
        score = score - 4;
      } else if (parameterType.equals(int.class)) {
        score = score - 3;
      } else if (parameterType.equals(Integer.class)) {
        score = score - 2;
      }
    }

    return new ScoreConstructor(score, c);
  }

  private ScoreConstructor[] scoreConstructors(Class<?> cls) {

    // find the constructor with the most number of parameters
    int maxParamCount = 0;

    Constructor<?>[] constructors = cls.getConstructors();

    ScoreConstructor[] score = new ScoreConstructor[constructors.length];

    for (int i = 0; i < constructors.length; i++) {
      score[i] = getScore(constructors[i]);
      if (score[i].hasDuplicateParamTypes()) {
        String msg = "Duplicate parameter types in " + score[i].constructor;
        throw new IllegalStateException(msg);
      }
      if (score[i].getParamCount() > maxParamCount) {
        maxParamCount = score[i].getParamCount();
      }
    }

    // filter out any constructors with less parameters than the max
    ArrayList<ScoreConstructor> list = new ArrayList<>();
    for (ScoreConstructor aScore : score) {
      if (aScore.getParamCount() == maxParamCount) {
        list.add(aScore);
      }
    }

    score = list.toArray(new ScoreConstructor[list.size()]);

    // sort into score ascending order
    Arrays.sort(score);

    return score;
  }


  private Method[] findGetters(Class<?> cls, Constructor<?> c) throws NoSuchMethodException {

    Method[] methods = cls.getMethods();

    Class<?>[] paramTypes = c.getParameterTypes();

    Method[] readers = new Method[paramTypes.length];

    for (int i = 0; i < paramTypes.length; i++) {
      Method getter = findGetter(paramTypes[i], methods);
      if (getter == null && paramTypes.length == 1 && paramTypes[i].equals(String.class)) {
        getter = findToString(cls);
      }
      if (getter == null) {
        throw new NoSuchMethodException("Get Method not found for " + paramTypes[i] + " in " + cls);
      }
      readers[i] = getter;
    }

    return readers;
  }

  private Method findToString(Class<?> cls) throws NoSuchMethodException {

    try {
      return cls.getDeclaredMethod("toString", new Class<?>[0]);
    } catch (SecurityException e) {
      throw new NoSuchMethodException("SecurityException " + e + " trying to find toString method on " + cls);
    }
  }

  private Method findGetter(Class<?> paramType, Method[] methods) {

    for (Method method : methods) {
      if (!Modifier.isStatic(method.getModifiers())) {
        if (method.getParameterTypes().length == 0) {
          // could be a getter
          String methName = method.getName();
          if (!methName.equals("hashCode") && !methName.equals("toString")) {
            Class<?> returnType = method.getReturnType();
            if (paramType.equals(returnType)) {
              return method;
            }
          }
        }
      }
    }
    return null;
  }

  private static class ScoreConstructor implements Comparable<ScoreConstructor> {

    final int score;
    final Constructor<?> constructor;

    private ScoreConstructor(int score, Constructor<?> constructor) {
      this.score = score;
      this.constructor = constructor;
    }

    @Override
    public boolean equals(Object obj) {
      // remove FindBugs warning
      return obj == this;
    }

    public int compareTo(ScoreConstructor o) {
      return (score < o.score ? -1 : (score == o.score ? 0 : 1));
    }

    public int getParamCount() {
      return constructor.getParameterTypes().length;
    }

    public boolean hasDuplicateParamTypes() {

      Class<?>[] parameterTypes = constructor.getParameterTypes();
      if (parameterTypes.length < 2) {
        return false;
      }
      HashSet<Class<?>> set = new HashSet<>();
      for (Class<?> parameterType : parameterTypes) {
        if (!set.add(parameterType)) {
          return true;
        }
      }
      return false;
    }

  }

}
