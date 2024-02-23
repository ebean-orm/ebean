package org.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PFile {

  @Id
  long id;
  String name;
}
