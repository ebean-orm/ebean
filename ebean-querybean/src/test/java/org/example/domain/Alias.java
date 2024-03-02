package org.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Alias {

  @Id
  long id;
  @Version
  long version;
}
