package io.ebean.bean.extend;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for a class to extend an existing class.
 * <p>
 * Normally, you would have annotated like the following example
 *
 * <pre>
 *   package basePkg;
 *   import extPkg.MyExtEntity;
 *   class MyBaseEntity {
 *     // this line is mandatory, to allow deletion of MyBaseEntity
 *     &#64;OneToOne(optional = true, cascade = Cascade.ALL)
 *     private MyExtEntity
 *   }
 *
 *   package extPkg;
 *   import basePkg.myBaseEntity;
 *   class MyExtEntity {
 *     &#64;OneToOne(optional = false)
 *     private MyBaseEntity
 *   }
 * </pre>
 * <p>
 * If you spread your code over different packages or (especially in different maven modules), you'll get problems, because you'll get cyclic depencencies.
 * <p>
 * To break up these dependencies, you can annotate 'MyExtEntity'
 *
 * <pre>
 *   package extPkg;
 *   import basePkg.myBaseEntity;
 *   &#64;EntityExtension(MyBaseEntity.class)
 *   class MyExtEntity {
 *     &#64;OneToOne(optional = false)
 *     private MyBaseEntity
 *
 *     private String someField;
 *   }
 * </pre>
 * This will create a virtual property in the MyBaseEntity without adding a dependency to MyExtEntity.
 * <p>
 * You may add a
 * <pre>
 *   public static MyExtEntity get(MyBaseEntity base) {
 *     throw new NotEnhancedException();
 *   }
 * </pre>
 * This getter will be replaced by the enhancer, so that you can easily get it with
 * <code>MyExtEntiy.get(base).getSomeField()</code>.
 * <br>
 * Technically, the instance of MyExtEntiy is stored in the <code>_ebean_extension_storage</code> array of
 * <code>MyBaseEntity</code>.
 * <p>
 * If you save the <code>MyBaseEntity</code>, it will also save the data stored in <code>MyExtEntity</code>.
 *
 * @author Alexander Wagner, FOCONIS AG
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface EntityExtension {
  Class<? extends ExtendableBean>[] value();
}
