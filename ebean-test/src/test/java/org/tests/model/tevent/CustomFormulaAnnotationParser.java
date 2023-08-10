package org.tests.model.tevent;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.ebean.annotation.Formula;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.plugin.CustomDeployParser;
import io.ebean.plugin.DeployBeanDescriptorMeta;
import io.ebean.plugin.DeployBeanPropertyMeta;
import io.ebean.util.AnnotationUtil;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;

/**
 * Custom Annotation parser which parses &#64;Count annotation
 *
 * @author Roland Praml, FOCONIS AG
  */
public class CustomFormulaAnnotationParser implements CustomDeployParser {

  private int counter;


  @Target(FIELD)
  @Retention(RUNTIME)
  @Formula(select="TODO", join = "TODO") // meta-formula
  public @interface Count {
    String value();
  }



  @Override
  public void parse(final DeployBeanDescriptorMeta descriptor, final DatabasePlatform databasePlatform) {
    for (DeployBeanPropertyMeta prop : descriptor.propertiesAll()) {
      readField(descriptor, prop);
    }
  }

  private void readField(DeployBeanDescriptorMeta descriptor, DeployBeanPropertyMeta prop) {
   Count countAnnot = AnnotationUtil.get(prop.getField(), Count.class);
    if (countAnnot != null) {
      // @Count found, so build the (complex) count formula
      DeployBeanPropertyAssocMany<?> countProp =  (DeployBeanPropertyAssocMany<?>) descriptor.getBeanProperty(countAnnot.value());
      counter++;
      String tmpTable = "f"+counter;
      String sqlSelect = "coalesce(" + tmpTable + ".child_count, 0)";
      String parentId = countProp.getMappedBy() + "_id";
      String tableName = countProp.getBeanTable().getBaseTable();
      String sqlJoin = "left join (select " + parentId +", count(*) as child_count from " + tableName + " GROUP BY " + parentId + " )"
          + " " + tmpTable + " on " + tmpTable + "." +parentId + " = ${ta}." + descriptor.idProperty().getDbColumn();
      prop.setSqlFormula(sqlSelect, sqlJoin);
//      prop.setSqlFormula("f1.child_count",
//          "join (select parent_id, count(*) as child_count from child_entity GROUP BY parent_id) f1 on f1.parent_id = ${ta}.id");
    }
  }

}
