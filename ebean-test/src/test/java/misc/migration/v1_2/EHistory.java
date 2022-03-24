package misc.migration.v1_2;


import io.ebean.annotation.Tablespace;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "migtest_e_history")
@Tablespace("db2;MAIN;")
public class EHistory {

  @Id
  Integer id;


  Long testString; // keep it as long as history prevents altering
}
