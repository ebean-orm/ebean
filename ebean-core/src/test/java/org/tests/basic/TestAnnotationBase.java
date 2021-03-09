package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.annotation.Index;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Where;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.IndexDefinition;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import org.junit.Test;
import org.tests.model.basic.ValidationGroupSomething;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static io.ebean.annotation.Platform.H2;
import static io.ebean.annotation.Platform.POSTGRES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TestAnnotationBase extends BaseTestCase {

  private final Set<Class<?>> metaAnnotationsFilter = new HashSet<>();

  public TestAnnotationBase() {
    metaAnnotationsFilter.add(Where.class);
    metaAnnotationsFilter.add(Where.List.class);
  }

  @Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Where(clause = "SELECT 'mysql' from 1", platforms = Platform.MYSQL)
  @Where(clause = "SELECT 'h2' from 1", platforms = H2)
  @Where(clause = "SELECT 'other' from 1")
  public @interface MetaTest {

  }

  @Index(name = "ano_1", columnNames = "direct", platforms = {H2, POSTGRES})
  @Index(name = "ano_2", columnNames = "direct", platforms = {H2, POSTGRES})
  @MappedSuperclass
  public static class MappedBaseEntity {

  }

  @Index(name = "ano_3", columnNames = "direct")
  @Entity
  public static class TestAnnotationBaseEntity extends MappedBaseEntity {

    @Where(clause = "SELECT 'mysql' from 1", platforms = Platform.MYSQL)
    @Where(clause = "SELECT 'h2' from 1", platforms = H2)
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

  @Entity
  public static class TestJakartaAnnotationBaseEntity extends MappedBaseEntity {
    @Where(clause = "SELECT 'mysql' from 1", platforms = Platform.MYSQL)
    @Where(clause = "SELECT 'h2' from 1", platforms = H2)
    @Where(clause = "SELECT 'other' from 1")
    private String direct;

    @MetaTest
    private String meta;

    @MetaTest
    @Where(clause = "SELECT 'oracle' from 1", platforms = Platform.ORACLE)
    private String mixed;

    @jakarta.validation.constraints.Size.List({
      @jakarta.validation.constraints.Size(max = 10, message = "max length for you is 10"),
      @jakarta.validation.constraints.Size(min = 1),
      @jakarta.validation.constraints.Size(max = 40, message = "max value for you is 40", groups = ValidationGroupSomething.class)
    })
    private String constraintAnnotation;

    @NotNull
    private String null1;


    @NotNull(groups = ValidationGroupSomething.class)
    private String null2;

    private String null3;

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
  public void testFindJakartaMaxSize() throws SecurityException {
    BeanDescriptor<TestJakartaAnnotationBaseEntity> descriptor = spiEbeanServer().getBeanDescriptor(TestJakartaAnnotationBaseEntity.class);
    BeanProperty bp = descriptor.findProperty("constraintAnnotation");
    assertEquals(40, bp.getDbLength());
  }

  @Test
  public void annotationClassIndexes() throws SecurityException {
    BeanDescriptor<TestAnnotationBaseEntity> descriptor = spiEbeanServer().getBeanDescriptor(TestAnnotationBaseEntity.class);
    final IndexDefinition[] indexDefinitions = descriptor.getIndexDefinitions();
    assertEquals(3, indexDefinitions.length);
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
  public void testJakartaNotNullWithGroup() throws SecurityException {
    BeanDescriptor<TestJakartaAnnotationBaseEntity> descriptor = spiEbeanServer().getBeanDescriptor(TestJakartaAnnotationBaseEntity.class);

    BeanProperty bp = descriptor.findProperty("null1");
    assertFalse(bp.isNullable());

    bp = descriptor.findProperty("null2");
    assertTrue(bp.isNullable());

    bp = descriptor.findProperty("null3");
    assertTrue(bp.isNullable());
  }

  @Test
  public void testFindAnnotation() throws NoSuchFieldException, SecurityException {

    Field directFld = TestAnnotationBaseEntity.class.getDeclaredField("direct");
    final DeployBeanProperty direct = createProperty(directFld);

    assertEquals("SELECT 'mysql' from 1", where(direct, Platform.MYSQL));
    assertEquals("SELECT 'h2' from 1", where(direct, H2));
    assertEquals("SELECT 'other' from 1", where(direct, Platform.POSTGRES));

    // meta
    Field metaFld = TestAnnotationBaseEntity.class.getDeclaredField("meta");
    final DeployBeanProperty meta = createProperty(metaFld);

    assertEquals("SELECT 'mysql' from 1", where(meta, Platform.MYSQL));
    assertEquals("SELECT 'h2' from 1", where(meta, H2));
    assertEquals("SELECT 'other' from 1", where(meta, Platform.POSTGRES));

    // mixed
    Field mixedFld = TestAnnotationBaseEntity.class.getDeclaredField("mixed");
    final DeployBeanProperty mixed = createProperty(mixedFld);

    assertEquals("SELECT 'mysql' from 1", where(mixed, Platform.MYSQL));
    assertEquals("SELECT 'h2' from 1", where(mixed, H2));
    assertEquals("SELECT 'other' from 1", where(mixed, Platform.POSTGRES));
    assertEquals("SELECT 'oracle' from 1", where(mixed, Platform.ORACLE));
  }

  private String where(DeployBeanProperty property, Platform platform) {
    return property.getMetaAnnotationWhere(platform).clause();
  }

  private DeployBeanProperty createProperty(Field fld) {
    DeployBeanProperty directProperty = new DeployBeanProperty(null, null, null);
    directProperty.setField(fld);
    directProperty.initMetaAnnotations(metaAnnotationsFilter);
    return directProperty;
  }

}
