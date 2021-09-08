package org.tests.inheritance.company.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * @author Per-Ingemar Andersson, It-huset i Norden AB
 */
@Entity
@Table(name = "foo")
public class Foo {
  @Id
  @GeneratedValue
  private int fooId;

  private String importantText;

  @Version
  private int version;

  public void setFooId(int fooId) {
    this.fooId = fooId;
  }

  public int getFooId() {
    return fooId;
  }

  public String getImportantText() {
    return importantText;
  }

  public void setImportantText(String importantText) {
    this.importantText = importantText;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }
}
