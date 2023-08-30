package org.tests.aggregateformula;

import jakarta.persistence.*;

@Entity
@Table(name = "iaf_segment")
// Inheritance
// DiscriminatorValue("target")
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
