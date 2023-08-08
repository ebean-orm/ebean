package org.tests.idkeys.db;

import io.ebean.Model;

import javax.persistence.*;

@Entity
@Table(name = "ea_object")
public class EaObject extends Model {

  @Id
  @Column(name = "object_id", nullable = false)
  @GeneratedValue(
    strategy = GenerationType.AUTO,
    generator = "t_object_sequence_generator"
  )
  @SequenceGenerator(
    name = "t_object_sequence_generator",
    sequenceName = "object_id_seq"
  )
  long id;

  final String name;

  public EaObject(String name) {
    this.name = name;
  }

  public long id() {
    return id;
  }

  public String name() {
    return name;
  }
}
