package org.example.domain;

import com.avaje.ebean.Ebean;
import org.postgis.Point;
import org.postgis.Polygon;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;

public class TestInsertQuery {

  @Test
  public void insert() throws SQLException {


    List<MyBean> list = Ebean.find(MyBean.class).findList();
    for (MyBean MyBean : list) {
      System.out.println(MyBean.getPoint());
    }

    List<MyBean> list1 = Ebean.find(MyBean.class)
        .where()
        .raw("st_within(st_pointfromwkb(st_point(?, ?), 4674), poly)", 1.9, 1.9)
        .findList();

    System.out.println(list1);


    Point point = new Point("SRID=4674;POINT(2.8 1.7)");
    Polygon poly =  new Polygon("SRID=4674;POLYGON((2 2, 2 -2, -2 -2, -2 2, 2 2))");

    MyBean p = new MyBean();
    p.setName("me at "+System.currentTimeMillis());
    p.setPoint(point);
    p.setPoly(poly);

    Ebean.save(p);
  }
}
