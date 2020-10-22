package org.example.domain;

import io.ebean.Ebean;
import org.geolatte.geom.Point;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.codec.Wkt;
import org.geolatte.geom.LineString;
import org.geolatte.geom.MultiLineString;
import org.geolatte.geom.MultiPoint;
import org.geolatte.geom.MultiPolygon;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;

public class TestOtherInsertQuery {

  /**
   * Not automated this test yet.
   */
  @Test(enabled = false)
  public void insert() throws SQLException {


    List<OtherBeanGeoLatte> list = Ebean.find(OtherBeanGeoLatte.class).findList();
    for (OtherBeanGeoLatte OtherBeanGeoLatte : list) {
      System.out.println(OtherBeanGeoLatte.getPoint());
    }

    List<OtherBeanGeoLatte> list1 = Ebean.find(OtherBeanGeoLatte.class)
        .where()
        .raw("st_within(st_pointfromwkb(st_point(?, ?), 4674), poly)", 1.9, 1.9)
        .findList();

    System.out.println(list1);


    Point point = (Point) Wkt.fromWkt("SRID=4674;POINT (12 23)");
    Polygon poly = (Polygon) Wkt.fromWkt("SRID=4674;POLYGON((2 2, 2 -2, -2 -2, -2 2, 2 2))");

    LineString lineString = (LineString)Wkt.fromWkt("SRID=4674;LINESTRING(0 0, 1 2)");
    MultiLineString multiLineString = (MultiLineString)Wkt.fromWkt("SRID=4674;MULTILINESTRING((0 0, 1 2), (1 2, 3 -1))");
    MultiPoint multiPoint = (MultiPoint)Wkt.fromWkt("SRID=4674;MULTIPOINT(10 40, 40 30, 20 20, 30 10)");
    MultiPolygon mpoly =  (MultiPolygon)Wkt.fromWkt("SRID=4674;MULTIPOLYGON(((1 1, 1 -1, -1 -1, -1 1, 1 1)),((1 1, 3 1, 3 3, 1 3, 1 1)))");

    OtherBeanGeoLatte p = new OtherBeanGeoLatte();
    p.setName("geolatte at "+System.currentTimeMillis());
    p.setPoint(point);
    p.setPoly(poly);
    p.setLineString(lineString);
    p.setMpoly(mpoly);
    p.setMultiPoint(multiPoint);
    p.setMultiLineString(multiLineString);

    Ebean.save(p);
  }
}
