package org.tests.model.generated;

import io.ebean.annotation.DbPartition;
import io.ebean.annotation.PartitionMode;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@DbPartition(mode = PartitionMode.MONTH, property = "eventTime")
@Entity
@Table(name="mypart_combo")
public class MyPart {

  @Id @GeneratedValue
  long id;

  final Instant eventTime;

  String metaInfo;

  public MyPart(Instant eventTime) {
    this.eventTime = eventTime;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Instant getEventTime() {
    return eventTime;
  }

  public String getMetaInfo() {
    return metaInfo;
  }

  public void setMetaInfo(String metaInfo) {
    this.metaInfo = metaInfo;
  }

}
