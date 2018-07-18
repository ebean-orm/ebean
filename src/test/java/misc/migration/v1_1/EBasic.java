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
import javax.validation.constraints.Size;

import java.sql.Timestamp;

@Entity
@Table(name = "migtest_e_basic")
@Index(columnNames  = { "status" , "indextest1"}, unique = true)
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
  @Size(max=127)
  String name;


  @DbMigration(preAlter = "-- rename all collisions")
  @Column(unique = true)
  @Size(max=127)
  String description;

  //@NotNull
  //@DbDefault("2000-01-01T00:00:00") //- date time literals do not work for each platform yet
  //@DbDefault("now") //- now does not work for mariaDb
  // MariaDb requires: ALTER TABLE `migtest_e_basic` CHANGE `some_date` `some_date` DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP;
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

  @Size(max=127)
  String indextest1;

  @Size(max=127)
  String indextest2;

  @Index
  @Size(max=127)
  String indextest3;

  @Index(unique = true)
  @Size(max=127)
  String indextest4;

  @Index(unique = true)
  @Size(max=127)
  String indextest5;

  @Index(unique = false)
  @Size(max=127)
  String indextest6;

  @NotNull
  @DbDefault("0")
  Progress progress;

  @DbDefault("42")
  int newInteger;

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
