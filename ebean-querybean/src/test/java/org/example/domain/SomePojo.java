package org.example.domain;

import java.util.ArrayList;

public class SomePojo {

  String name;

  ArrayList<String> foos = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
