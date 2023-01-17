package org.tests.idkeys.db;

import io.ebean.Model;

import javax.persistence.*;

@Entity
@Table(name = "T_OBJECT")
public class EaObject extends Model {

  @Id
  @Column(name = "OBJECT_ID", nullable = false)
  @GeneratedValue(
    strategy = GenerationType.AUTO,
    generator = "t_object_sequence_generator"
  )
  @SequenceGenerator(
    name = "t_object_sequence_generator",
    sequenceName = "OBJECT_ID_SEQ"
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
