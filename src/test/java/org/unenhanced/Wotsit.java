package org.unenhanced;

import javax.persistence.Entity;
import javax.persistence.Id;

// Not in org.example, i.e. not enhanced at build time
@Entity
public class Wotsit {
  @Id
  public String name;
}
