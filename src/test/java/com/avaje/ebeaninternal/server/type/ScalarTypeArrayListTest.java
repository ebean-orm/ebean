package com.avaje.ebeaninternal.server.type;


import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLException;

public class ScalarTypeArrayListTest {

  @Test
  public void read_when_null() throws SQLException {

    DataReader mock = Mockito.mock(DataReader.class);
    Mockito.when(mock.getArray()).thenReturn(null);

    ScalarTypeArrayList scalarType = ScalarTypeArrayList.typeFor(Long.class);
    scalarType.read(mock);

  }
}