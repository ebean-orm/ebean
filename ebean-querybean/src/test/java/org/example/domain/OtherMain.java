package org.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import org.example.domain.other.OtherBean;

@SuppressWarnings("unused")
@Entity
public class OtherMain {

  @Id
  long id;

  @ManyToOne
  OtherBean other;

}
