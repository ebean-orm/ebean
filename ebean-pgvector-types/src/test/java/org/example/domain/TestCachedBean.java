package org.example.domain;

import com.pgvector.PGbit;
import com.pgvector.PGvector;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestCachedBean {
  @Test
  public void testCache() {
    var v=new PGvector(TestInsertQuery.randomVector(800));
    var b=new PGbit((TestInsertQuery.randomBitArray(200)));
    CachedBean cb=new CachedBean();
    cb.setName("test");
    cb.setVector(v);
    cb.setBit(b);
    DB.insert(cb);

    CachedBean r1=DB.find(CachedBean.class, cb.getId());
    assertNotNull(r1);
    assertEquals(v, r1.getVector());
    assertEquals(b, r1.getBit());

    CachedBean r2=DB.find(CachedBean.class, cb.getId());
    assertNotNull(r2);
    assertEquals(v, r2.getVector());
    assertEquals(b, r2.getBit());

    DB.delete(r2);
  }
}
