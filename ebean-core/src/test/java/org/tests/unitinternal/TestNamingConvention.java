package org.tests.unitinternal;

import junit.framework.TestCase;

public class TestNamingConvention extends TestCase {

  /**
   * Test dummy method to allow rest to be commented out.
   */
  public void testDummy() {

  }

  // Comment out for now so the entities do not get registered
  // Run this manually as needed.

//    @Entity
//    @Table(name = "table1", schema = "annotationschema")
//    public class FooBar1 {
//    }
//
//    @Entity
//    @Table(name = "table2")
//    public class FooBar2 {
//    }
//
//    @Entity
//    public class FooBar3 {
//    }
//
//    public AbstractNamingConvention namingConvention;
//
//    public void setUp() {
//        namingConvention = new UnderscoreNamingConvention();
//        namingConvention.setDatabasePlatform(new DatabasePlatform());
//        namingConvention.setSchema("conventionschema");
//    }
//
//    public void test_table_name_and_schema_name_from_annotation() {
//        Assert.assertNotNull(FooBar1.class.getAnnotation(Table.class));
//        Assert.assertEquals("annotationschema.table1", namingConvention.getTableName(FooBar1.class).getQualifiedName());
//    }
//
//    public void test_table_name_from_annotation_and_schema_name_by_convention() {
//        Assert.assertNotNull(FooBar2.class.getAnnotation(Table.class));
//        Assert.assertEquals("conventionschema.table2", namingConvention.getTableName(FooBar2.class).getQualifiedName());
//    }
//
//    public void test_table_name_and_schema_name_by_convention() {
//        Assert.assertNull(FooBar3.class.getAnnotation(Table.class));
//        Assert.assertEquals("conventionschema.foo_bar3", namingConvention.getTableName(FooBar3.class)
//                .getQualifiedName());
//    }

}
