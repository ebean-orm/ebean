package org.tests.aggregateformula;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;

@Entity
@Inheritance
@DiscriminatorValue("target")
public class IAFPartialSegment extends IAFBaseSegment {

  private final long segmentIdZat;

  @ManyToOne(optional = false)
  private final IAFSegmentStatus status;

  public IAFPartialSegment(long segmentIdZat, IAFSegmentStatus status) {
    this.segmentIdZat = segmentIdZat;
    this.status = status;
  }

  public long getSegmentIdZat() {
    return segmentIdZat;
  }

  public IAFSegmentStatus getStatus() {
    return status;
  }
}
