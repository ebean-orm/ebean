package org.tests.inheritance.company.domain;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

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
