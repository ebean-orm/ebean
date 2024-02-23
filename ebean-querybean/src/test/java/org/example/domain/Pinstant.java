package org.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Pinstant {

  @Id
  long id;
  String name;
}
