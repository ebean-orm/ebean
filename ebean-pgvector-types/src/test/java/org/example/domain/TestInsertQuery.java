package org.example.domain;

import com.pgvector.PGbit;
import com.pgvector.PGhalfvec;
import com.pgvector.PGsparsevec;
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

  static boolean compareHalfVectors(PGhalfvec v1, PGhalfvec v2) {
    if (v1==null || v2==null) return v1==v2;
    float[] a1=v1.toArray();
    float[] a2=v2.toArray();
    if (a1.length != a2.length) return false;
    for (int i = 0; i < a1.length; i++) {
      if (Math.abs(a1[i]-a2[i])>0.001) return false;
    }
    return true;
  }

  static float[] randomSparseVector(int dim, int nonZeroCount) {
    Random rnd = new Random();
    float[] vector = new float[dim];
    for (int i = 0; i < nonZeroCount; i++) {
      int index;
      do {
        index = rnd.nextInt(dim);
      } while (vector[index] != 0);
      vector[index] = rnd.nextFloat();
    }
    return vector;
  }

  static boolean[] randomBitArray(int length) {
    Random rnd = new Random();
    boolean[] bits = new boolean[length];
    for (int i = 0; i < length; i++) {
      bits[i] = rnd.nextBoolean();
    }
    return bits;
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

  @Test
  public void differentTypes() throws SQLException {
    var rv1=new PGvector(randomVector(200));
    var rv2=new PGvector(randomVector(200));
    var rh1=new PGhalfvec(randomVector(420));
    var rh2=new PGhalfvec(randomVector(420));
    var rb1=new PGbit(randomBitArray(1200));
    var rb2=new PGbit(randomBitArray(1200));
    var rs1=new PGsparsevec(randomSparseVector(350, 2));
    var rs2=new PGsparsevec(randomSparseVector(350, 2));

    MyBean b1=new MyBean();
    b1.setName("testTypes");
    b1.setVector(rv1);
    b1.setHalfvec(rh1);
    b1.setBit(rb1);
    b1.setSparse(rs1);
    DB.save(b1);

    MyBean b2=new MyBean();
    b2.setName("testTypes2");
    b2.setVector(rv2);
    b2.setHalfvec(rh2);
    b2.setBit(rb2);
    b2.setSparse(rs2);
    DB.save(b2);

    var f1=DB.find(MyBean.class).where().eq("vector", rv1).findOne();
    assertNotNull(f1);
    assertEquals(b1.getId(), f1.getId());
    assertEquals(b1.getVector(), f1.getVector());
    assertTrue(compareHalfVectors(b1.getHalfvec(), f1.getHalfvec()));
    assertEquals(b1.getBit(), f1.getBit());
    assertEquals(b1.getSparse(), f1.getSparse());

    var f2=DB.find(MyBean.class).where().eq("sparse", rs2).findOne();
    assertNotNull(f2);
    assertEquals(b2.getId(), f2.getId());
    assertEquals(b2.getVector(), f2.getVector());
    assertTrue(compareHalfVectors(b2.getHalfvec(), f2.getHalfvec()));
    assertEquals(b2.getBit(), f2.getBit());
    assertEquals(b2.getSparse(), f2.getSparse());

    var f3=DB.find(MyBean.class).where().eq("bit", rb1).findOne();
    assertNotNull(f3);
    assertEquals(b1.getId(), f3.getId());
    assertEquals(b1.getVector(), f3.getVector());
    assertTrue(compareHalfVectors(b1.getHalfvec(), f3.getHalfvec()));
    assertEquals(b1.getBit(), f3.getBit());
    assertEquals(b1.getSparse(), f3.getSparse());

    DB.delete(f1);

    assertEquals(1, DB.find(MyBean.class).where().eq("halfvec", rh2).delete());
    assertNull(DB.find(MyBean.class).where().eq("sparse", rs2).findOne());
    assertNull(DB.find(MyBean.class).where().eq("bit", rb1).findOne());

  }
}
