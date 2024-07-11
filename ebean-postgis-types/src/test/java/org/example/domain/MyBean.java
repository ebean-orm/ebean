package org.example.domain;

import net.postgis.jdbc.geometry.LineString;
import net.postgis.jdbc.geometry.MultiLineString;
import net.postgis.jdbc.geometry.MultiPoint;
import net.postgis.jdbc.geometry.MultiPolygon;
import net.postgis.jdbc.geometry.Point;
import net.postgis.jdbc.geometry.Polygon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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
