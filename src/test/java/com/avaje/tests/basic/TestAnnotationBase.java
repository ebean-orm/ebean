package com.avaje.tests.basic;

import static org.junit.Assert.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.annotation.EbeanDDL;
import com.avaje.ebean.annotation.Where;
import com.avaje.ebean.config.dbplatform.H2Platform;
import com.avaje.ebean.config.dbplatform.MySqlPlatform;
import com.avaje.ebean.config.dbplatform.OraclePlatform;
import com.avaje.ebean.config.dbplatform.PostgresPlatform;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.parse.AnnotationBase;
import com.avaje.tests.model.basic.ValidationGroupSomething;


public class TestAnnotationBase extends BaseTestCase {

  @Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
  @Retention(RetentionPolicy.RUNTIME)
  @Where(clause = "SELECT 'mysql' from 1", platforms = MySqlPlatform.class)
  @Where(clause = "SELECT 'h2' from 1", platforms = H2Platform.class)
  @Where(clause = "SELECT 'other' from 1")
  public @interface MetaTest {

  }

  @Entity
  public static class TestAnnotationBaseEntity {

    @Where(clause = "SELECT 'mysql' from 1", platforms = MySqlPlatform.class)
    @Where(clause = "SELECT 'h2' from 1", platforms = H2Platform.class)
    @Where(clause = "SELECT 'other' from 1")
    private String direct;

    @MetaTest
    private String meta;


    @MetaTest
    @Where(clause = "SELECT 'oracle' from 1", platforms = OraclePlatform.class)
    private String mixed;

    @Size.List({
      @Size(max=10, message="max length for you is 10"),
      @Size(min=1),
      @Size(max=40, message="max value for you is 40", groups = ValidationGroupSomething.class)
    })
    private String constraintAnnotation;

    @NotNull
    private String null1;


    @NotNull(groups = ValidationGroupSomething.class)
    private String null2;

    @NotNull(groups = {ValidationGroupSomething.class, EbeanDDL.class})
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
  public void testFindMaxSize() throws NoSuchFieldException, SecurityException {
    BeanDescriptor<TestAnnotationBaseEntity> descriptor = spiEbeanServer().getBeanDescriptor(TestAnnotationBaseEntity.class);
    BeanProperty bp = descriptor.findBeanProperty("constraintAnnotation");
    assertEquals(40, bp.getDbLength());
  }

  @Test
  public void testNotNullWithGroup() throws NoSuchFieldException, SecurityException {
    BeanDescriptor<TestAnnotationBaseEntity> descriptor = spiEbeanServer().getBeanDescriptor(TestAnnotationBaseEntity.class);

    BeanProperty bp = descriptor.findBeanProperty("null1");
    assertFalse(bp.isNullable());

    bp = descriptor.findBeanProperty("null2");
    assertTrue(bp.isNullable());

    bp = descriptor.findBeanProperty("null3");
    assertFalse(bp.isNullable());
  }
  
  @Test
  public void testFindAnnotation() throws NoSuchFieldException, SecurityException {
    Field fld = TestAnnotationBaseEntity.class.getDeclaredField("direct");
    String s;
        
    s= AnnotationBase.findAnnotation(fld, Where.class, MySqlPlatform.class).clause();
    assertEquals("SELECT 'mysql' from 1",s);
    
    s= AnnotationBase.findAnnotation(fld, Where.class, H2Platform.class).clause();
    assertEquals("SELECT 'h2' from 1",s);
    
    s= AnnotationBase.findAnnotation(fld, Where.class, PostgresPlatform.class).clause();
    assertEquals("SELECT 'other' from 1",s);
    
    // meta
    fld = TestAnnotationBaseEntity.class.getDeclaredField("meta");
    
    s= AnnotationBase.findAnnotation(fld, Where.class, MySqlPlatform.class).clause();
    assertEquals("SELECT 'mysql' from 1",s);
    
    s= AnnotationBase.findAnnotation(fld, Where.class, H2Platform.class).clause();
    assertEquals("SELECT 'h2' from 1",s);
    
    s= AnnotationBase.findAnnotation(fld, Where.class, PostgresPlatform.class).clause();
    assertEquals("SELECT 'other' from 1",s);
    
   
    // mixed
    fld = TestAnnotationBaseEntity.class.getDeclaredField("mixed");
    
    s= AnnotationBase.findAnnotation(fld, Where.class, MySqlPlatform.class).clause();
    assertEquals("SELECT 'mysql' from 1",s);
    
    s= AnnotationBase.findAnnotation(fld, Where.class, H2Platform.class).clause();
    assertEquals("SELECT 'h2' from 1",s);
    
    s= AnnotationBase.findAnnotation(fld, Where.class, PostgresPlatform.class).clause();
    assertEquals("SELECT 'other' from 1",s);

    s= AnnotationBase.findAnnotation(fld, Where.class, OraclePlatform.class).clause();
    assertEquals("SELECT 'oracle' from 1",s);
  }

}
