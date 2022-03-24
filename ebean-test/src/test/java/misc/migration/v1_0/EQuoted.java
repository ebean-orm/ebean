package misc.migration.v1_0;

import io.ebean.annotation.EnumValue;
import io.ebean.annotation.Index;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

// table with upper and lower case letters
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
