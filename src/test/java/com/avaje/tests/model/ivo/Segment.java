package com.avaje.tests.model.ivo;

/**
 * Simple example of String based IVO.
 */
public class Segment {

  private final String code;

  public Segment(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public String toString() {
    return code;
  }

}
