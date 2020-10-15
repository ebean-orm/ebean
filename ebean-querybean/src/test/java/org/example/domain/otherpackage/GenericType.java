package org.example.domain.otherpackage;

public class GenericType<T> {
  private T data;

  public GenericType(final T data) {
    this.data = data;
  }

  public T getData() {
    return data;
  }

  public void setData(final T data) {
    this.data = data;
  }
}
