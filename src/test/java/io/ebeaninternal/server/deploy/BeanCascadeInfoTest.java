package io.ebeaninternal.server.deploy;

import org.junit.Test;

import javax.persistence.CascadeType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BeanCascadeInfoTest {

  @Test
  public void setTypes_ALL() throws Exception {

    BeanCascadeInfo info = new BeanCascadeInfo();
    info.setTypes(new CascadeType[]{CascadeType.ALL});
    assertTrue(info.isSave());
    assertTrue(info.isDelete());
    assertTrue(info.isRefresh());
  }

  @Test
  public void setTypes_PERSIST() throws Exception {

    BeanCascadeInfo info = new BeanCascadeInfo();
    info.setTypes(new CascadeType[]{CascadeType.PERSIST});
    assertTrue(info.isSave());
    assertFalse(info.isDelete());
    assertFalse(info.isRefresh());
  }

  @Test
  public void setTypes_MERGE() throws Exception {

    BeanCascadeInfo info = new BeanCascadeInfo();
    info.setTypes(new CascadeType[]{CascadeType.MERGE});
    assertTrue(info.isSave());
    assertFalse(info.isDelete());
    assertFalse(info.isRefresh());
  }

  @Test
  public void setTypes_REMOVE() throws Exception {

    BeanCascadeInfo info = new BeanCascadeInfo();
    info.setTypes(new CascadeType[]{CascadeType.REMOVE});
    assertFalse(info.isSave());
    assertTrue(info.isDelete());
    assertFalse(info.isRefresh());
  }

  @Test
  public void setTypes_REFRESH() throws Exception {

    BeanCascadeInfo info = new BeanCascadeInfo();
    info.setTypes(new CascadeType[]{CascadeType.REFRESH});
    assertFalse(info.isSave());
    assertFalse(info.isDelete());
    assertTrue(info.isRefresh());
  }

  @Test
  public void setDelete() throws Exception {

    BeanCascadeInfo info = new BeanCascadeInfo();
    info.setDelete(true);
    assertFalse(info.isSave());
    assertTrue(info.isDelete());
    assertFalse(info.isRefresh());
  }

  @Test
  public void setSaveDelete() throws Exception {

    BeanCascadeInfo info = new BeanCascadeInfo();
    info.setSaveDelete(true, true);
    assertTrue(info.isSave());
    assertTrue(info.isDelete());
    assertFalse(info.isRefresh());
  }

}
