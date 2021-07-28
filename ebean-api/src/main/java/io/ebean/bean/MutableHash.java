package io.ebean.bean;


public interface MutableHash {
  
  boolean isEqualToJson(String json);

  default boolean isEqualToObject(Object obj) {
    return true;
  }
  
  default Object get() {
    return null;
  }
}
 