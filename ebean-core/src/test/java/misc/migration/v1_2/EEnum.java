package misc.migration.v1_2;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.ebean.annotation.EnumValue;

@Entity
@Table(name = "migtest_e_enum")
public class EEnum {
  
  public enum Status {
    @EnumValue("N")
    NEW,

    @EnumValue("A")
    ACTIVE,

    @EnumValue("I")
    INACTIVE,
  }
  
  @Id
  Integer id;
  
  Status testStatus;
}
