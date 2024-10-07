package io.ebean.querybean.generator;

public interface LangAdapter {


  void alias(Append writer, String shortName, String beanFullName);

  void rootBeanConstructor(Append writer, String shortName, String dbName, String beanFullName);

  void assocBeanConstructor(Append writer, String shortName);

  void fetch(Append writer, String origShortName);

  void fieldDefn(Append writer, String propertyName, String typeDefn);

}
