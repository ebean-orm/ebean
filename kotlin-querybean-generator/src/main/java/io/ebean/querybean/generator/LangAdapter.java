package io.ebean.querybean.generator;

public interface LangAdapter {

  void beginClass(Append writer, String shortName);

  void beginAssocClass(Append writer, String shortName, String origShortName);

  void alias(Append writer, String shortName);

  void rootBeanConstructor(Append writer, String shortName, String dbName);

  void assocBeanConstructor(Append writer, String shortName);

  void fetch(Append writer, String origShortName);

  void fieldDefn(Append writer, String propertyName, String typeDefn);

}
