package org.tests.iud;

import io.ebean.Model;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
public class PcfModel extends Model {

  @Id
  long id;

  @Version
  long version;

}
