package org.tests.mockito;

import org.junit.Test;
import org.mockito.Mockito;
import org.tests.model.aggregation.DOrg;
import org.tests.model.basic.PersistentFile;

public class TestMockitoMock {

  @Test
  public void mockEntityBean() {

    final DOrg mock = Mockito.mock(DOrg.class);
    mock.setName("junk");
    mock.save();

    final PersistentFile mock1 = Mockito.mock(PersistentFile.class);
    mock1.setName("foo");
  }

  @Test
  public void spyEntityBean() {

    final PersistentFile spy = Mockito.spy(PersistentFile.class);
    spy.setName("foo");
    spy.setVersion(42L);

    final PersistentFile verify = Mockito.verify(spy);
    verify.setName("foo");
    verify.setVersion(42L);
  }

  @Test
  public void spyInstanceEntityBean() {

    PersistentFile file = new PersistentFile();
    final PersistentFile spy = Mockito.spy(file);
    spy.setName("foo");
    spy.setVersion(42L);

    final PersistentFile verify = Mockito.verify(spy);
    verify.setName("foo");
    verify.setVersion(42L);
  }
}

