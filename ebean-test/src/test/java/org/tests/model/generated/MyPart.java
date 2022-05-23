package org.tests.model.generated;

import io.ebean.annotation.DbPartition;
import io.ebean.annotation.PartitionMode;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
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
