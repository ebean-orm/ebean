package org.example.domain;

import io.ebean.DB;
import net.postgis.jdbc.geometry.Point;
import net.postgis.jdbc.geometry.Polygon;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class TestCacheSerialization {
  @Test
  public void testCache() throws SQLException {
    Point p1=new Point(1.0, 2.0);
    p1.setSrid(4674);

    Polygon pol=new Polygon("SRID=4674;POLYGON((0 0, 10 0, 10 10, 0 10, 0 0),(1 1, 1 2, 2 2, 2 1, 1 1))");

    CachedBean tb=new CachedBean();
    tb.setId(2080L);
    tb.setName("Serialize Test");
    tb.setPoint(p1);
    tb.setPolygon(pol);

    DB.insert(tb);

    CachedBean r1=DB.find(CachedBean.class, tb.getId());
    assertNotNull(r1);
    assertEquals(p1, r1.getPoint());
    assertEquals(pol, r1.getPolygon());

    CachedBean r2=DB.find(CachedBean.class, tb.getId());  // this is returned from cache
    assertNotNull(r2);
    assertEquals(p1, r2.getPoint());  // and did fail https://github.com/ebean-orm/ebean/issues/3026
    assertEquals(pol, r2.getPolygon());

    DB.delete(tb);

  }

  @Test
  public void testNullCache() throws SQLException {
    Point p1=new Point(1.0, 2.0);
    p1.setSrid(4674);
    Polygon pol=new Polygon("SRID=4674;POLYGON((0 0, 10 0, 10 10, 0 10, 0 0),(1 1, 1 2, 2 2, 2 1, 1 1))");

    CachedBean tb=new CachedBean();
    tb.setId(2081L);
    tb.setName("Serialize Test");
    tb.setPoint(p1);
    tb.setPolygon(null);

    DB.insert(tb);

    CachedBean r1=DB.find(CachedBean.class, tb.getId());
    assertNotNull(r1);
    assertEquals(p1, r1.getPoint());
    assertNull(r1.getPolygon());

    CachedBean r2=DB.find(CachedBean.class, tb.getId());  // this is returned from cache
    assertNotNull(r2);
    assertEquals(p1, r2.getPoint());  // and did fail https://github.com/ebean-orm/ebean/issues/3026
    assertNull(r1.getPolygon());

    tb.setPoint(null);
    tb.setPolygon(pol);

    DB.save(tb);

    r1=DB.find(CachedBean.class, tb.getId());
    assertNotNull(r1);
    assertEquals(pol, r1.getPolygon());
    assertNull(r1.getPoint());

    r2=DB.find(CachedBean.class, tb.getId());  // this is returned from cache
    assertNotNull(r2);
    assertEquals(pol, r2.getPolygon());  // and did fail https://github.com/ebean-orm/ebean/issues/3026
    assertNull(r1.getPoint());

    DB.delete(tb);

  }
}
