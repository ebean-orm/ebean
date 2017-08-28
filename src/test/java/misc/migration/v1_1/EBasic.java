package misc.migration.v1_1;

import io.ebean.Platform;
import io.ebean.annotation.DdlMigration;
import io.ebean.annotation.EnumValue;
import io.ebean.annotation.Index;
import io.ebean.annotation.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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

  @NotNull
  @DdlMigration(defaultValue="A")
  Status status;

  @Index(unique = true)
  String name;

  @DdlMigration(preDdl = "-- clean up uniqueness")
  @Column(unique = true)
  String description;

  @NotNull
  @DdlMigration(defaultValue="'2000-01-01T00:00:00'")
  Timestamp someDate;
  
  @NotNull
  @DdlMigration(defaultValue="foo")
  String newStringField;

  @NotNull
  @DdlMigration(defaultValue="true", postDdl = "update ${table} set ${column} = old_boolean")
  Boolean newBooleanField;

  @NotNull
  @DdlMigration(defaultValue="true")
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
