package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Where;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import org.junit.Test;
import org.tests.model.basic.ValidationGroupSomething;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TestAnnotationBase extends BaseTestCase {

  @Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Where(clause = "SELECT 'mysql' from 1", platforms = Platform.MYSQL)
  @Where(clause = "SELECT 'h2' from 1", platforms = Platform.H2)
  @Where(clause = "SELECT 'other' from 1")
  public @interface MetaTest {

  }

  @Entity
  public static class TestAnnotationBaseEntity {

    @Where(clause = "SELECT 'mysql' from 1", platforms = Platform.MYSQL)
    @Where(clause = "SELECT 'h2' from 1", platforms = Platform.H2)
    @Where(clause = "SELECT 'other' from 1")
    private String direct;

    @MetaTest
    private String meta;


    @MetaTest
    @Where(clause = "SELECT 'oracle' from 1", platforms = Platform.ORACLE)
    private String mixed;

    @Size.List({
      @Size(max = 10, message = "max length for you is 10"),
      @Size(min = 1),
      @Size(max = 40, message = "max value for you is 40", groups = ValidationGroupSomething.class)
    })
    private String constraintAnnotation;

    @NotNull
    private String null1;


    @NotNull(groups = ValidationGroupSomething.class)
    private String null2;

    private String null3;

    public String getDirect() {
      return direct;
    }

    public void setDirect(String direct) {
      this.direct = direct;
    }

    public String getMeta() {
      return meta;
    }

    public void setMeta(String meta) {
      this.meta = meta;
    }

    public String getMixed() {
      return mixed;
    }

    public void setMixed(String mixed) {
      this.mixed = mixed;
    }

    public String getConstraintAnnotation() {
      return constraintAnnotation;
    }

    public void setConstraintAnnotation(String constraintAnnotation) {
      this.constraintAnnotation = constraintAnnotation;
    }

    public String getNull1() {
      return null1;
    }

    public void setNull1(String null1) {
      this.null1 = null1;
    }

    public String getNull2() {
      return null2;
    }

    public void setNull2(String null2) {
      this.null2 = null2;
    }

    public String getNull3() {
      return null3;
    }

    public void setNull3(String null3) {
      this.null3 = null3;
    }
  }


  @Test
  public void testFindMaxSize() throws SecurityException {
    BeanDescriptor<TestAnnotationBaseEntity> descriptor = spiEbeanServer().getBeanDescriptor(TestAnnotationBaseEntity.class);
    BeanProperty bp = descriptor.findProperty("constraintAnnotation");
    assertEquals(40, bp.getDbLength());
  }

  @Test
  public void testNotNullWithGroup() throws SecurityException {
    BeanDescriptor<TestAnnotationBaseEntity> descriptor = spiEbeanServer().getBeanDescriptor(TestAnnotationBaseEntity.class);

    BeanProperty bp = descriptor.findProperty("null1");
    assertFalse(bp.isNullable());

    bp = descriptor.findProperty("null2");
    assertTrue(bp.isNullable());

    bp = descriptor.findProperty("null3");
    assertTrue(bp.isNullable());
  }

  @Test
  public void testFindAnnotation() throws NoSuchFieldException, SecurityException {
    Field fld = TestAnnotationBaseEntity.class.getDeclaredField("direct");
    String s;

    s = AnnotationUtil.findAnnotation(fld, Where.class, Platform.MYSQL).clause();
    assertEquals("SELECT 'mysql' from 1", s);

    s = AnnotationUtil.findAnnotation(fld, Where.class, Platform.H2).clause();
    assertEquals("SELECT 'h2' from 1", s);

    s = AnnotationUtil.findAnnotation(fld, Where.class, Platform.POSTGRES).clause();
    assertEquals("SELECT 'other' from 1", s);

    // meta
    fld = TestAnnotationBaseEntity.class.getDeclaredField("meta");

    s = AnnotationUtil.findAnnotation(fld, Where.class, Platform.MYSQL).clause();
    assertEquals("SELECT 'mysql' from 1", s);

    s = AnnotationUtil.findAnnotation(fld, Where.class, Platform.H2).clause();
    assertEquals("SELECT 'h2' from 1", s);

    s = AnnotationUtil.findAnnotation(fld, Where.class, Platform.POSTGRES).clause();
    assertEquals("SELECT 'other' from 1", s);


    // mixed
    fld = TestAnnotationBaseEntity.class.getDeclaredField("mixed");

    s = AnnotationUtil.findAnnotation(fld, Where.class, Platform.MYSQL).clause();
    assertEquals("SELECT 'mysql' from 1", s);

    s = AnnotationUtil.findAnnotation(fld, Where.class, Platform.H2).clause();
    assertEquals("SELECT 'h2' from 1", s);

    s = AnnotationUtil.findAnnotation(fld, Where.class, Platform.POSTGRES).clause();
    assertEquals("SELECT 'other' from 1", s);

    s = AnnotationUtil.findAnnotation(fld, Where.class, Platform.ORACLE).clause();
    assertEquals("SELECT 'oracle' from 1", s);
  }

}
