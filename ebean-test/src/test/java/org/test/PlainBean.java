package org.test;

import java.util.Objects;

public class PlainBean {

  public int id;
  public String name;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PlainBean plainBean = (PlainBean) o;
    return id == plainBean.id && name.equals(plainBean.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }
}
