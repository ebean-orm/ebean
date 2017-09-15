package misc.migration.v1_1;

import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbMigration;
import io.ebean.annotation.EnumValue;
import io.ebean.annotation.Index;
import io.ebean.annotation.NotNull;

import javax.persistence.Column;
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

    @EnumValue("?")
    DONT_KNOW,
  }

  public enum Progress {
    @EnumValue("0")
    START,

    @EnumValue("1")
    RUN,

    @EnumValue("2")
    END
  }

  @Id
  Integer id;

  @NotNull
  @DbDefault("A")
  Status status;

  @Index(unique = true)
  String name;


  @DbMigration(preAlter = "-- rename all collisions")
  @Column(unique = true)
  String description;

  @NotNull
  @DbDefault("2000-01-01T00:00:00")
  Timestamp someDate;

  @NotNull
  @DbDefault("foo'bar")
  String newStringField;

  @NotNull
  @DbDefault("true")
  @DbMigration(postAdd = "update ${table} set ${column} = old_boolean")
  Boolean newBooleanField;

  @NotNull
  @DbDefault("true")
  boolean newBooleanField2;

  String indextest1;

  String indextest2;

  @Index
  String indextest3;

  @Index(unique = true)
  String indextest4;

  @Index(unique = true)
  String indextest5;

  @Index(unique = false)
  String indextest6;

  @NotNull
  @DbDefault("0")
  Progress progress;

  @DbDefault("42")
  int newInteger;

  @NotNull
  @ManyToOne
  @DbMigration(preAlter= "insert into migtest_e_user (id) select distinct user_id from migtest_e_basic") // ensure all users exist
  EUser user;

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

  public String getNewStringField() {
    return newStringField;
  }

  public void setNewStringField(String newStringField) {
    this.newStringField = newStringField;
  }
}
