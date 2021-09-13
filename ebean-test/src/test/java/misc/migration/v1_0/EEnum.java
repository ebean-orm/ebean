package misc.migration.v1_0;


import io.ebean.annotation.EnumValue;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
