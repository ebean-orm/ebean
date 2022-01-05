package org.tests.model.basic;

public interface TWithPreInsertCommon {

  Integer getId();

  String getName();

  void setName(String name);

  String getTitle();

  void setTitle(String title);

  void requestCascadeState(int requestCascadeState);
}
