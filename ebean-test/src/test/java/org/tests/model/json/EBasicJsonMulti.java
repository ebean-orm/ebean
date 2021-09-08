package org.tests.model.json;

import io.ebean.Model;
import io.ebean.annotation.DbJson;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import static io.ebean.annotation.MutationDetection.SOURCE;

@Entity
public class EBasicJsonMulti extends Model {

  @Id
  Long id;

  String name;

  @DbJson(length = 500, mutationDetection = SOURCE)
  PlainBeanDirtyAware plainValue1;

  @DbJson(length = 500, mutationDetection = SOURCE)
  PlainBeanDirtyAware plainValue2;

  @DbJson(length = 500, mutationDetection = SOURCE)
  PlainBeanDirtyAware plainValue3;

  @Version
  long version;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PlainBeanDirtyAware getPlainValue1() {
    return plainValue1;
  }

  public void setPlainValue1(PlainBeanDirtyAware plainValue1) {
    this.plainValue1 = plainValue1;
  }

  public PlainBeanDirtyAware getPlainValue2() {
    return plainValue2;
  }

  public void setPlainValue2(PlainBeanDirtyAware plainValue2) {
    this.plainValue2 = plainValue2;
  }

  public PlainBeanDirtyAware getPlainValue3() {
    return plainValue3;
  }

  public void setPlainValue3(PlainBeanDirtyAware plainValue3) {
    this.plainValue3 = plainValue3;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
