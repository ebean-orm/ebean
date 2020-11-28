package io.ebeaninternal.server.type;

import io.ebean.core.type.ScalarType;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

class BasePlatformArrayTypeFactoryTest {

  void assertBindNullTo_Null(ScalarType<?> type) throws SQLException {

    TestDoubleDataBind bind = new TestDoubleDataBind();
    type.bind(bind, null);
    assertTrue(bind.wasSetNull);
  }

  void assertBindNullTo_EmptyArray(ScalarType<?> type) throws SQLException {

    TestDoubleDataBind bind = new TestDoubleDataBind();
    type.bind(bind, null);

    assertTrue(bind.wasEmptyArray);
  }

  void assertBindNullTo_EmptyString(ScalarType<?> type) throws SQLException {

    TestDoubleDataBind bind = new TestDoubleDataBind();
    type.bind(bind, null);
    assertTrue(bind.setEmptyString);
  }

  void assertBindNullTo_PGObjectNull(ScalarType<?> type) throws SQLException {

    TestDoubleDataBind bind = new TestDoubleDataBind();
    type.bind(bind, null);
    assertTrue(bind.wasPgoNull);
  }
  void assertBindNullTo_PGObjectEmpty(ScalarType<?> type) throws SQLException {

    TestDoubleDataBind bind = new TestDoubleDataBind();
    type.bind(bind, null);
    assertTrue(bind.wasPgoEmpty);
  }

  static class TestDoubleDataBind extends DataBind {

    boolean setEmptyString;
    boolean wasSetNull;
    boolean setArray;
    boolean wasNull;
    boolean wasEmptyArray;
    boolean wasPgoNull;
    boolean wasPgoEmpty;

    TestDoubleDataBind() {
      super(null, null, null);
    }

    @Override
    public void setNull(int jdbcType) {
      wasSetNull = true;
    }

    @Override
    public void setString(String s) {
      setEmptyString = "[]".equals(s);
    }

    @Override
    public void setObject(Object value) {
      wasNull = value == null;
      if (!wasNull) {
        if (value instanceof Object[]) {
          wasEmptyArray = ((Object[]) value).length == 0;
        } else {
          PGobject pgo = (PGobject) value;
          wasPgoEmpty = "[]".equals(pgo.getValue());
          wasPgoNull = pgo.getValue() ==  null;
        }
      }
    }

    @Override
    public void setArray(String arrayType, Object[] elements) {
      setArray = true;
      wasNull = elements == null;
      wasEmptyArray = (elements != null && elements.length == 0);
    }
  }

}
