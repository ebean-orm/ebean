package io.ebean.bean;

public interface MutableJson {
  
  boolean isEqualToObject(Object obj);
  
  boolean isEqualToJson(String json);
  
  Object get();

  void update(Object obj);
}
