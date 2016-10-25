package org.example.domain;


import org.geolatte.geom.LineString;
import org.geolatte.geom.MultiLineString;
import org.geolatte.geom.MultiPoint;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.Point;
import org.geolatte.geom.Polygon;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="mybean")
public class OtherBeanGeoLatte extends BaseEntity {

  String name;

  Point point;

  Polygon poly;

  LineString lineString;

  MultiLineString multiLineString;

  MultiPoint multiPoint;

  MultiPolygon mpoly;

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

  public MultiPolygon getMpoly() {
    return mpoly;
  }

  public void setMpoly(MultiPolygon mpoly) {
    this.mpoly = mpoly;
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
}
