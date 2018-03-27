package org.tests.model.ddd;

import org.tests.model.ivo.Oid;
import org.tests.model.ivo.converter.AnEnumType;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.sql.Timestamp;

@Entity
public class DExhEntity {

  @Id
  Oid<DExhEntity> oid;

  String exhange;

  AnEnumType anEnumType;

  @Version
  Timestamp lastUpdated;

  public Oid<DExhEntity> getOid() {
    return oid;
  }

  public void setOid(Oid<DExhEntity> oid) {
    this.oid = oid;
  }

  public String getExhange() {
    return exhange;
  }

  public void setExhange(String exhange) {
    this.exhange = exhange;
  }

  public Timestamp getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Timestamp lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public AnEnumType getAnEnumType() {
    return anEnumType;
  }

  public void setAnEnumType(AnEnumType anEnumType) {
    this.anEnumType = anEnumType;
  }
}
