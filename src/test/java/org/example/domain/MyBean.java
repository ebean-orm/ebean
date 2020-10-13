package org.example.domain;

import org.postgis.LineString;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.MultiPolygon;
import org.postgis.Point;
import org.postgis.Polygon;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="mybean")
public class MyBean extends BaseEntity {

  String name;

  Point point;

  Polygon poly;

  LineString lineString;

  MultiLineString multiLineString;

  MultiPoint multiPoint;

  MultiPolygon mpoly;

  @Column(columnDefinition = "geometry(Point, 4326)", unique = true)
  Point wgs84Point;

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

  public Polygon getPoly() {
    return poly;
  }

  public void setPoly(Polygon poly) {
    this.poly = poly;
  }

  public LineString getLineString() {
    return lineString;
  }

  public void setLineString(LineString lineString) {
    this.lineString = lineString;
  }

  public MultiLineString getMultiLineString() {
    return multiLineString;
  }

  public void setMultiLineString(MultiLineString multiLineString) {
    this.multiLineString = multiLineString;
  }

  public MultiPoint getMultiPoint() {
    return multiPoint;
  }

  public void setMultiPoint(MultiPoint multiPoint) {
    this.multiPoint = multiPoint;
  }

  public MultiPolygon getMpoly() {
    return mpoly;
  }

  public void setMpoly(MultiPolygon mpoly) {
    this.mpoly = mpoly;
  }

  public Point getWgs84Point() {
    return wgs84Point;
  }

  public void setWgs84Point(final Point wgs84Point) {
    this.wgs84Point = wgs84Point;
  }
}
