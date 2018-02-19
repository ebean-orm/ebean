package misc.migration.v1_0;

import io.ebean.annotation.EnumValue;
import io.ebean.annotation.Index;
import io.ebean.annotation.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import java.sql.Timestamp;

@Entity
@Table(name = "migtest_e_basic")
public class EBasic {

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

  Status status;

  @Size(max=127)
  String name;

  @Size(max=127)
  String description;

  Timestamp someDate;
  
  boolean old_boolean;

  Boolean old_boolean2;
  
  @ManyToOne
  ERef eref;


  // test add & remove indices
  @Index
  @Size(max=127)
  String indextest1;

  @Index(unique = true)
  @Size(max=127)
  String indextest2;

  @Size(max=127)
  String indextest3;

  @Size(max=127)
  String indextest4;

  @Index(unique = false)
  @Size(max=127)
  String indextest5;

  @Index(unique = true)
  @Size(max=127)
  String indextest6;

  @NotNull
  int user_id;

  public EBasic() {

  }

  public EBasic(String name) {
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Timestamp getSomeDate() {
    return someDate;
  }

  public void setSomeDate(Timestamp someDate) {
    this.someDate = someDate;
  }

}
