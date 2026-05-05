package org.example.domain;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.postgis.LineString;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.MultiPolygon;
import org.postgis.Point;
import org.postgis.Polygon;

import java.sql.SQLException;
import java.util.List;

class TestInsertQuery {

  /**
   * Not automated this test yet.
   */
  @Test
  void insert() throws SQLException {

    List<MyBean> list = DB.find(MyBean.class).findList();
    for (MyBean MyBean : list) {
      System.out.println(MyBean.getPoint());
    }

    List<MyBean> list1 = DB.find(MyBean.class)
        .where()
        .raw("st_within(st_pointfromwkb(st_point(?, ?), 4674), poly)", 1.9, 1.9)
        .findList();

    System.out.println(list1);


    Point point = new Point("SRID=4674;POINT(2.8 1.7)");
    Polygon poly =  new Polygon("SRID=4674;POLYGON((2 2, 2 -2, -2 -2, -2 2, 2 2))");
    LineString lineString = new LineString("SRID=4674;LINESTRING(0 0, 1 2)");
    MultiLineString multiLineString = new MultiLineString("SRID=4674;MULTILINESTRING((0 0, 1 2), (1 2, 3 -1))");
    MultiPoint multiPoint = new MultiPoint("SRID=4674;MULTIPOINT((2 3), (7 8))");
    MultiPolygon mpoly =  new MultiPolygon("SRID=4674;MULTIPOLYGON(((1 1, 1 -1, -1 -1, -1 1, 1 1)),((1 1, 3 1, 3 3, 1 3, 1 1)))");

    MyBean p = new MyBean();
    p.setName("me at "+System.currentTimeMillis());
    p.setPoint(point);
    p.setPoly(poly);
    p.setLineString(lineString);
    p.setMultiLineString(multiLineString);
    p.setMultiPoint(multiPoint);
    p.setMpoly(mpoly);

    DB.save(p);

    List<String> content =
      DB.find(MyBean.class)
      .select("ST_AsText(lineString)::String")
      .findSingleAttributeList();

    System.out.println(content);
  }
}
