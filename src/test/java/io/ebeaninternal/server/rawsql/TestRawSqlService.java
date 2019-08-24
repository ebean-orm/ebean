package io.ebeaninternal.server.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.SqlRow;
import io.ebeaninternal.server.rawsql.SpiRawSql.Sql;
import org.assertj.core.api.StrictAssertions;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.rawsql.ERawSqlAggBean;

import com.mysql.cj.protocol.Resultset;

import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class TestRawSqlService extends BaseTestCase {

  @Test
  public void testDistinctColumnNames() throws SQLException {

    ResultSetMetaData rsetmeta = Mockito.mock(ResultSetMetaData.class);
    ResultSet rset = Mockito.mock(ResultSet.class);

    Mockito.when(rsetmeta.getColumnCount()).thenReturn(3);
    Mockito.when(rset.getMetaData()).thenReturn(rsetmeta);
    for (int i = 1; i < 4; i++) {
      Mockito.when(rsetmeta.getColumnLabel(i)).thenReturn(null);
      Mockito.when(rsetmeta.getColumnName(i)).thenReturn("col" + i);
      Mockito.when(rsetmeta.getSchemaName(i)).thenReturn("schema" + i);
      Mockito.when(rsetmeta.getTableName(i)).thenReturn("table" + i);

      Mockito.when(rset.getObject(i)).thenReturn("dat1_" + i).thenReturn("dat2_" + i);
    }

    DRawSqlService service = new DRawSqlService();
    SqlRow result = service.sqlRow(rset, "1", false);
    
    assertThat(result.keySet()).contains("col1", "col2", "col3");
    for (int i = 1; i < 4; i++) {
      assertThat(result.get("col" + i)).isEqualTo("dat1_" + i);
    }

    result = service.sqlRow(rset, "1", false);

    assertThat(result.keySet()).contains("col1", "col2", "col3");
    for (int i = 1; i < 4; i++) {
      assertThat(result.get("col" + i)).isEqualTo("dat2_" + i);
    }
  }

  @Test
  public void testDistinctColumnLabels() throws SQLException {

    ResultSetMetaData rsetmeta = Mockito.mock(ResultSetMetaData.class);
    ResultSet rset = Mockito.mock(ResultSet.class);

    Mockito.when(rsetmeta.getColumnCount()).thenReturn(3);
    Mockito.when(rset.getMetaData()).thenReturn(rsetmeta);
    for (int i = 1; i < 4; i++) {
      Mockito.when(rsetmeta.getColumnLabel(i)).thenReturn("label" + i);
      Mockito.when(rsetmeta.getColumnName(i)).thenReturn("col" + i);
      Mockito.when(rsetmeta.getSchemaName(i)).thenReturn("schema" + i);
      Mockito.when(rsetmeta.getTableName(i)).thenReturn("table" + i);

      Mockito.when(rset.getObject(i)).thenReturn("dat1_" + i).thenReturn("dat2_" + i);
    }

    DRawSqlService service = new DRawSqlService();
    SqlRow result = service.sqlRow(rset, "1", false);

    assertThat(result.keySet()).contains("label1", "label2", "label3");
    for (int i = 1; i < 4; i++) {
      assertThat(result.get("label" + i)).isEqualTo("dat1_" + i);
    }

    result = service.sqlRow(rset, "1", false);

    assertThat(result.keySet()).contains("label1", "label2", "label3");
    for (int i = 1; i < 4; i++) {
      assertThat(result.get("label" + i)).isEqualTo("dat2_" + i);
    }
  }

  @Test
  public void testIdenticalColumnNames() throws SQLException {

    ResultSetMetaData rsetmeta = Mockito.mock(ResultSetMetaData.class);
    ResultSet rset = Mockito.mock(ResultSet.class);

    Mockito.when(rsetmeta.getColumnCount()).thenReturn(3);
    Mockito.when(rset.getMetaData()).thenReturn(rsetmeta);
    for (int i = 1; i < 4; i++) {
      Mockito.when(rsetmeta.getColumnLabel(i)).thenReturn(null);
      Mockito.when(rsetmeta.getColumnName(i)).thenReturn("col");
      Mockito.when(rsetmeta.getSchemaName(i)).thenReturn("schema" + i);
      Mockito.when(rsetmeta.getTableName(i)).thenReturn("table" + i);

      Mockito.when(rset.getObject(i)).thenReturn("dat1_" + i).thenReturn("dat2_" + i);
    }

    DRawSqlService service = new DRawSqlService();
    SqlRow result = service.sqlRow(rset, "1", false);

    assertThat(result.keySet()).contains("col", "schema2.table2.col", "schema3.table3.col");
    assertThat(result.get("col")).isEqualTo("dat1_1");
    assertThat(result.get("schema2.table2.col")).isEqualTo("dat1_2");
    assertThat(result.get("schema3.table3.col")).isEqualTo("dat1_3");

    result = service.sqlRow(rset, "1", false);

    assertThat(result.keySet()).contains("col", "schema2.table2.col", "schema3.table3.col");
    assertThat(result.get("col")).isEqualTo("dat2_1");
    assertThat(result.get("schema2.table2.col")).isEqualTo("dat2_2");
    assertThat(result.get("schema3.table3.col")).isEqualTo("dat2_3");
  }

  @Test
  public void testIdenticalColumnLabels() throws SQLException {

    ResultSetMetaData rsetmeta = Mockito.mock(ResultSetMetaData.class);
    ResultSet rset = Mockito.mock(ResultSet.class);

    Mockito.when(rsetmeta.getColumnCount()).thenReturn(3);
    Mockito.when(rset.getMetaData()).thenReturn(rsetmeta);
    for (int i = 1; i < 4; i++) {
      Mockito.when(rsetmeta.getColumnLabel(i)).thenReturn("label");
      Mockito.when(rsetmeta.getColumnName(i)).thenReturn("col");
      Mockito.when(rsetmeta.getSchemaName(i)).thenReturn("schema" + i);
      Mockito.when(rsetmeta.getTableName(i)).thenReturn("table" + i);

      Mockito.when(rset.getObject(i)).thenReturn("dat1_" + i).thenReturn("dat2_" + i);
    }

    DRawSqlService service = new DRawSqlService();
    SqlRow result = service.sqlRow(rset, "1", false);

    assertThat(result.keySet()).contains("label", "schema2.table2.label", "schema3.table3.label");
    assertThat(result.get("label")).isEqualTo("dat1_1");
    assertThat(result.get("schema2.table2.label")).isEqualTo("dat1_2");
    assertThat(result.get("schema3.table3.label")).isEqualTo("dat1_3");

    result = service.sqlRow(rset, "1", false);

    assertThat(result.keySet()).contains("label", "schema2.table2.label", "schema3.table3.label");
    assertThat(result.get("label")).isEqualTo("dat2_1");
    assertThat(result.get("schema2.table2.label")).isEqualTo("dat2_2");
    assertThat(result.get("schema3.table3.label")).isEqualTo("dat2_3");
  }

}
