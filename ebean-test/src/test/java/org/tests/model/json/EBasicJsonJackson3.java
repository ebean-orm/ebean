package org.tests.model.json;

import io.ebean.Model;
import io.ebean.annotation.DbJson;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

import static io.ebean.annotation.MutationDetection.NONE;
import static io.ebean.annotation.MutationDetection.SOURCE;

@Entity
public class EBasicJsonJackson3 extends Model {

  @Id
  Long id;

  String name;

  @DbJson(length = 500, mutationDetection = SOURCE)
  PlainBeanDirtyAware plainValue;

  @DbJson(length = 500)
  PlainBeanDirtyAware plainValue2;

  @DbJson(length = 500, mutationDetection = NONE)
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

  public PlainBeanDirtyAware getPlainValue() {
    return plainValue;
  }

  public void setPlainValue(PlainBeanDirtyAware plainValue) {
    this.plainValue = plainValue;
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
