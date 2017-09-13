package misc.migration.v1_2;

import io.ebean.annotation.DbDefault;
import io.ebean.annotation.EnumValue;
import io.ebean.annotation.Index;
import io.ebean.annotation.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
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
  
  String name;

  String description;

  Timestamp someDate;
  
  boolean old_boolean;

  Boolean old_boolean2;
  
  @ManyToOne
  ERef eref;
  
  
  // test add & remove indices
  @Index
  String indextest1;
  
  @Index(unique = true)
  String indextest2;
  
  String indextest3;
  
  String indextest4;
  
  @Index(unique = false)
  String indextest5;
  
  @Index(unique = true)
  String indextest6;
  
  @NotNull
  @DbDefault("23")
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
