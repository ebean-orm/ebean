package org.example.domain;

import com.pgvector.PGvector;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TestInsertQuery {
  static float[] randomVector(int dim) {
    Random rnd = new Random();
    float[] vector = new float[dim];
    for (int i = 0; i < dim; i++) {
      vector[i] = rnd.nextFloat();
    }
    return vector;
  }

  @Test
  public void insert() throws SQLException {
    List<MyBean> list = DB.find(MyBean.class).findList();
    for (MyBean MyBean : list) {
      System.out.println(MyBean.getVector());
    }

    var v1=new PGvector(randomVector(200));

    MyBean myBean=new MyBean();
    myBean.setName("test");
    myBean.setVector(v1);
    DB.save(myBean);

    var dbBean=DB.find(MyBean.class, myBean.getId());
    assertNotNull(dbBean);
    assertEquals(myBean.getVector().toString(), dbBean.getVector().toString());

  }
}
