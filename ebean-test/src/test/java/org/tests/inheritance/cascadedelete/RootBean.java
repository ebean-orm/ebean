package org.tests.inheritance.cascadedelete;

import javax.persistence.*;
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
