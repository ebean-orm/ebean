package org.tests.inheritance.cascadedelete;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.PreRemove;
import java.util.UUID;

@Entity
@Inheritance
public abstract class RootBean {

  @Id
  @GeneratedValue
  public UUID id;

  // added as workaround for the issue
  @PreRemove
  public void preRemove() {
  }
}
