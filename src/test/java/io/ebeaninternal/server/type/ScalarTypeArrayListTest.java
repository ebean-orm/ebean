package io.ebeaninternal.server.type;


import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;

public class ScalarTypeArrayListTest {

  @Test
  public void read_when_null() throws SQLException {

    DataReader mock = Mockito.mock(DataReader.class);
    Mockito.when(mock.getArray()).thenReturn(null);

    ScalarType<?> scalarType = ScalarTypeArrayList.factory().typeFor(Long.class);
    scalarType.read(mock);
  }
}
