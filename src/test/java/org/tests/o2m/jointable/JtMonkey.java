package org.tests.o2m.jointable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="monkey")
public class JtMonkey {

  @Id
  long mid;

  String name;

  String foodPreference;

  @Version
  long version;

  public JtMonkey(String name) {
    this.name = name;
  }

  public long getMid() {
    return mid;
  }

  public void setMid(long mid) {
    this.mid = mid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFoodPreference() {
    return foodPreference;
  }

  public void setFoodPreference(String foodPreference) {
    this.foodPreference = foodPreference;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
