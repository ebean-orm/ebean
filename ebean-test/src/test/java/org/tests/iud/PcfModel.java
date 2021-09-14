package org.tests.iud;

import io.ebean.Model;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

@MappedSuperclass
public class PcfModel extends Model {

  @Id
  long id;

  @Version
  long version;

}
