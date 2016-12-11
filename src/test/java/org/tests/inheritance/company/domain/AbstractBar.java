package org.tests.inheritance.company.domain;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * @author Per-Ingemar Andersson, It-huset i Norden AB
 */

@Entity
@Table(name = "bar")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "bar_type", discriminatorType = DiscriminatorType.STRING)
//@MappedSuperclass
public abstract class AbstractBar {

  @Id
  @GeneratedValue
  private int barId;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "foo_id", nullable = false)
  private Foo foo;

  @Version
  private int version;

  public void setBarId(int barId) {
    this.barId = barId;
  }

  public int getBarId() {
    return barId;
  }

  public Foo getFoo() {
    return foo;
  }

  public void setFoo(Foo foo) {
    this.foo = foo;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }
}
