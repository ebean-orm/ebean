package org.example.domain;

import io.ebean.annotation.Cache;
import org.postgis.Point;
import org.postgis.Polygon;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="mybean_cached")
@Cache
public class CachedBean extends BaseEntity {
  String name;

  Point point;

  Polygon polygon;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Point getPoint() {
    return point;
  }

  public void setPoint(Point point) {
    this.point = point;
  }

  public Polygon getPolygon() {
    return polygon;
  }

  public void setPolygon(Polygon polygon) {
    this.polygon = polygon;
  }
}
