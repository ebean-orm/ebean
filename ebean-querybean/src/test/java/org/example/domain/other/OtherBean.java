package org.example.domain.other;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@SuppressWarnings("unused")
@Entity
public class OtherBean {

  @Id
  long id;
  String name;
}
