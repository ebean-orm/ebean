package org.example.domain;

import io.ebean.DB;
import org.geolatte.geom.G2D;
import org.geolatte.geom.LineString;
import org.geolatte.geom.MultiLineString;
import org.geolatte.geom.MultiPoint;
import org.geolatte.geom.MultiPolygon;
import org.geolatte.geom.Point;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.PositionSequenceBuilders;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.geolatte.geom.codec.Wkt.toWkt;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

public class TestQuery {

  private final Point<G2D> wgs84Point = new Point<>(
    new G2D(174.76345825195312, -36.85315704345703),
    CoordinateReferenceSystems.WGS84
  );

  private final Point<G2D> geolatteClosePoint = new Point<>(
    new G2D(174.75345825195312, -36.85315704345703),
    CoordinateReferenceSystems.WGS84
  );

  private final Polygon<G2D> enclosingPolygon = new Polygon<>(
    PositionSequenceBuilders
      .variableSized(G2D.class)
      .add(new G2D(174.70345825195312, -36.85315704345703))
      .add(new G2D(174.79345825195312, -36.80315704345703))
      .add(new G2D(174.79345825195312, -36.89315704345703))
      .add(new G2D(174.70345825195312, -36.85315704345703))
      .toPositionSequence(),
    CoordinateReferenceSystems.WGS84
  );

  private final LineString<G2D> lineString = new LineString<>(
    PositionSequenceBuilders
      .variableSized(G2D.class)
      .add(new G2D(174.70345825195312, -36.85315704345703))
      .add(wgs84Point.getPosition())
      .add(new G2D(174.79345825195312, -36.89315704345703))
      .toPositionSequence(),
    CoordinateReferenceSystems.WGS84
  );

  @SuppressWarnings("unchecked")
  private final MultiPoint<G2D> multiPoint = new MultiPoint<>(
    new Point<>(new G2D(174.70345825195312, -36.85315704345703), CoordinateReferenceSystems.WGS84),
    wgs84Point,
    new Point<>(new G2D(174.79345825195312, -36.89315704345703), CoordinateReferenceSystems.WGS84)
  );

  @SuppressWarnings("unchecked")
  private final MultiPolygon<G2D> multiPolygon = new MultiPolygon<>(
    enclosingPolygon,
    new Polygon<>(
      PositionSequenceBuilders
        .variableSized(G2D.class)
        .add(new G2D(0, 51.47))
        .add(new G2D(0.01, 51.49))
        .add(new G2D(-0.01, 51.49))
        .add(new G2D(0, 51.47))
        .toPositionSequence(),
      CoordinateReferenceSystems.WGS84
    )
  );

  @SuppressWarnings("unchecked")
  private final MultiLineString<G2D> multiLineString = new MultiLineString<>(
    lineString,
    new LineString<>(
      PositionSequenceBuilders
        .variableSized(G2D.class)
        .add(new G2D(0, 51.47))
        .add(new G2D(0.01, 51.49))
        .add(new G2D(-0.01, 51.49))
        .add(new G2D(-0.00, 51.47))
        .toPositionSequence(),
      CoordinateReferenceSystems.WGS84
    )
  );

  @BeforeTest
  public void setupObjects() {
    OtherBeanGeoLatte geoLatte = new OtherBeanGeoLatte();
    geoLatte.setWgs84Point(wgs84Point);
    DB.save(geoLatte);
  }

  @AfterTest
  public void cleanup() {
    DB
      .find(MyBean.class)
      .delete();
    DB.find(OtherBeanGeoLatte.class)
      .delete();
  }

  @Test
  public void testGeoQueryGeoLatte() {
    // Approx 1100 meters away
    // 0.009999999999990905 degrees away

    System.err.println(
      DB
        .sqlQuery("SELECT st_distance(wgs84_point, ?), * FROM mybean")
        .setParameter(toWkt(geolatteClosePoint))
        .findOne()
    );

    assertNotNull(
      DB
        .find(OtherBeanGeoLatte.class)
        .where()
        .raw("st_distance(wgs84Point, ?) < 0.011", toWkt(geolatteClosePoint))
        .raw("st_distance(wgs84Point, ?) < 0.011 ", geolatteClosePoint)
        .findOne()
    );

    assertNull(
      DB
        .find(OtherBeanGeoLatte.class)
        .where()
        .raw("st_distance(wgs84Point, ?) < 0.009", toWkt(geolatteClosePoint))
        .raw("st_distance(wgs84Point, ?) < 0.009", geolatteClosePoint)
        .findOne()
    );


    assertNotNull(
      DB.find(OtherBeanGeoLatte.class)
        .where()
        .raw("ST_Within(wgs84Point, ST_GeomFromText(?))", toWkt(enclosingPolygon))
        .raw("ST_Within(wgs84Point, ?)", enclosingPolygon)
        .findOne()
    );

    assertNotNull(
      DB.find(OtherBeanGeoLatte.class)
        .where()
        .raw("wgs84Point = ST_PointN(ST_GeomFromText(?), 2)", toWkt(lineString))
        .raw("wgs84Point = ST_PointN(?, 2)", lineString)
        .findOne()
    );

    assertNotNull(
      DB.find(OtherBeanGeoLatte.class)
        .where()
        .raw("wgs84Point = ST_PointN(ST_LineFromMultiPoint(ST_GeomFromText(?)), 2)", toWkt(multiPoint))
        .raw("wgs84Point = ST_PointN(ST_LineFromMultiPoint(?), 2)", multiPoint)
        .findOne()
    );

    assertNotNull(
      DB.find(OtherBeanGeoLatte.class)
        .where()
        .raw("ST_Within(wgs84Point, ST_GeomFromText(?))", toWkt(multiPolygon))
        .raw("ST_Within(wgs84Point, ?)", multiPolygon)
        .findOne()
    );

    assertNotNull(
      DB.find(OtherBeanGeoLatte.class)
        .where()
        .raw("ST_Intersects(wgs84Point, ST_GeomFromText(?))", toWkt(multiLineString))
        .raw("ST_Intersects(wgs84Point, ?)", multiLineString)
        .findOne()
    );
  }

  @Test
  public void testGeoQueryPostgis() throws Exception {

    assertNotNull(
      DB
        .find(MyBean.class)
        .where()
        .raw("st_distance(wgs84Point, ?) < 0.011", toWkt(geolatteClosePoint))
        .raw("st_distance(wgs84Point, ?) < 0.011", new org.postgis.Point(toWkt(geolatteClosePoint)))
        .findOne()
    );

    assertNull(
      DB
        .find(MyBean.class)
        .where()
        .raw("st_distance(wgs84Point, ?) < 0.009", toWkt(geolatteClosePoint))
        .raw("st_distance(wgs84Point, ?) < 0.009", new org.postgis.Point(toWkt(geolatteClosePoint)))
        .findOne()
    );

    assertNotNull(
      DB
        .find(MyBean.class)
        .where()
        .raw("ST_Within(wgs84Point, ST_GeomFromText(?))", toWkt(enclosingPolygon))
        .raw("ST_Within(wgs84Point, ?)", new org.postgis.Polygon(toWkt(enclosingPolygon)))
        .findOne()
    );

    assertNotNull(
      DB.find(MyBean.class)
        .where()
        .raw("wgs84Point = ST_PointN(ST_GeomFromText(?), 2)", toWkt(lineString))
        .raw("wgs84Point = ST_PointN(?, 2)", new org.postgis.LineString(toWkt(lineString)))
        .findOne()
    );

    assertNotNull(
      DB.find(OtherBeanGeoLatte.class)
        .where()
        .raw("wgs84Point = ST_PointN(ST_LineFromMultiPoint(ST_GeomFromText(?)), 2)", toWkt(multiPoint))
        .raw("wgs84Point = ST_PointN(ST_LineFromMultiPoint(?), 2)", new org.postgis.MultiPoint(toWkt(multiPoint)))
        .findOne()
    );

    assertNotNull(
      DB.find(OtherBeanGeoLatte.class)
        .where()
        .raw("wgs84Point = ST_PointN(ST_LineFromMultiPoint(ST_GeomFromText(?)), 2)", toWkt(multiPoint))
        .raw("wgs84Point = ST_PointN(ST_LineFromMultiPoint(?), 2)", new org.postgis.MultiPoint(toWkt(multiPoint)))
        .findOne()
    );

    assertNotNull(
      DB
        .find(MyBean.class)
        .where()
        .raw("ST_Within(wgs84Point, ST_GeomFromText(?))", toWkt(multiPolygon))
        .raw("ST_Within(wgs84Point, ?)", new org.postgis.MultiPolygon(toWkt(multiPolygon)))
        .findOne()
    );

    assertNotNull(
      DB.find(OtherBeanGeoLatte.class)
        .where()
        .raw("ST_Intersects(wgs84Point, ST_GeomFromText(?))", toWkt(multiLineString))
        .raw("ST_Intersects(wgs84Point, ?)", new org.postgis.MultiLineString(toWkt(multiLineString)))
        .findOne()
    );
  }
}
