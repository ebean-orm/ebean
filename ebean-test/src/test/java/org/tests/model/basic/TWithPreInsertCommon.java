package org.tests.model.basic;

public interface TWithPreInsertCommon {

  String getName();

  void setName(String name);

  String getTitle();

  void setTitle(String title);

  void requestCascadeState(int requestCascadeState);
}
