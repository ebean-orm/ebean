package org.example.domain;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.Ignore;
import org.testng.annotations.Test;

import io.ebean.DB;

public class TestQuery {
  @Ignore
  @Test
  public void testGeoQueryGeoLatte() {
    OtherBeanGeoLatte mb = new OtherBeanGeoLatte();
    mb.setWgs84Point(
      new Point<>(
        new G2D(174.76345825195312, -36.85315704345703),
        CoordinateReferenceSystems.WGS84
      )
    );
    DB.save(mb);

    // Approx 1100 meters away
    // 0.009999999999990905 degrees away
    Point<G2D> closePoint = new Point<>(
      new G2D(174.75345825195312, -36.85315704345703),
      CoordinateReferenceSystems.WGS84
    );

    assertNotNull(
      DB
      .find(OtherBeanGeoLatte.class)
      .where()
//      .raw("st_distance(wgs84Point, ?) < 0.011", Wkt.newEncoder().encode(closePoint))
          .raw("st_distance(wgs84Point, ?) < 0.011 ", closePoint)
      .findOne()
    );

    assertNull(
      DB
        .find(OtherBeanGeoLatte.class)
        .where()
//        .raw("st_distance(wgs84Point, ?) < 0.009", Wkt.newEncoder().encode(closePoint))
        .raw("st_distance(wgs84Point, ?) < 0.009", closePoint)
        .findOne()
    );
  }

  @Ignore
  @Test
  public void testGeoQueryPostgis() {
    MyBean mb = new MyBean();
    final org.postgis.Point wgs84Point = new org.postgis.Point(174.76345825195312, -36.85315704345703);
    wgs84Point.setSrid(4326);
    mb.setWgs84Point(wgs84Point);
    DB.save(mb);

    org.postgis.Point closePoint = new org.postgis.Point(174.75345825195312, -36.85315704345703);

    assertNotNull(
      DB
      .find(MyBean.class)
      .where()
//      .raw("st_distance(wgs84Point, ?) < 0.011", toText(closePoint))
      .raw("st_distance(wgs84Point, ?) < 0.011", closePoint)
      .findOne()
    );

    assertNull(
      DB
        .find(MyBean.class)
        .where()
//        .raw("st_distance(wgs84Point, ?) < 0.009", toText(closePoint))
        .raw("st_distance(wgs84Point, ?) < 0.009", closePoint)
        .findOne()
    );
  }

  public String toText(org.postgis.Point wgs84Point) {
    return "SRID=4326;POINT(" + wgs84Point.getX() + " " + wgs84Point.getY() + ")";
  }
}
