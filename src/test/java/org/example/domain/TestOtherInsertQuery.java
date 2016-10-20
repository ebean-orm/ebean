package org.example.domain;

import com.avaje.ebean.Ebean;
import org.geolatte.geom.Point;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.codec.Wkt;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;

public class TestOtherInsertQuery {

  @Test
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

    OtherBeanGeoLatte p = new OtherBeanGeoLatte();
    p.setName("geolatte at "+System.currentTimeMillis());
    p.setPoint(point);
    p.setPoly(poly);

    Ebean.save(p);
  }
}
