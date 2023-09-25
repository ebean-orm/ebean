package misc.migration.v1_2;

import io.ebean.annotation.EnumValue;
import io.ebean.annotation.Index;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

//table with upper and lower case letters
@Table(name = "`migtest_QuOtEd`")
@Entity
public class EQuoted {

  public enum Status {
    @EnumValue("N")
    NEW,

    @EnumValue("A")
    ACTIVE,

    @EnumValue("I")
    INACTIVE,
  }

  @Id
  private String id;

  @Index
  private Status status1;

  @Index(unique = true)
  private Status status2;

}
